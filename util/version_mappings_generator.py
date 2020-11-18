#  Minosoft
#  Copyright (C) 2020 Moritz Zwerger
#
#  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
#
#  This software is not affiliated with Mojang AB, the original developer of Minecraft.

import hashlib
import os
import re
import shutil
import tarfile
import traceback

import requests
import ujson

print("Minecraft mappings downloader (and generator)")

PRE_FLATTENING_UPDATE_VERSION = "17w46a"
DATA_FOLDER = "../data/resources/"
TEMP_FOLDER = DATA_FOLDER + "tmp/"
OPTIONAL_FILES_PER_VERSION = ["entities.json"]
FILES_PER_VERSION = ["blocks.json", "registries.json"] + OPTIONAL_FILES_PER_VERSION
DOWNLOAD_BASE_URL = "https://apimon.de/mcdata/"
RESOURCE_MAPPINGS_INDEX = ujson.load(open("../src/main/resources/assets/mapping/resources.json"))
MOJANG_MINOSOFT_FIELD_MAPPINGS = ujson.load(open("entitiesFieldMojangMinosoftMappings.json"))
VERSION_MANIFEST = requests.get('https://launchermeta.mojang.com/mc/game/version_manifest.json').json()
VERBOSE_LOG = False
DEFAULT_MAPPINGS = ujson.load(open("mappingsDefaults.json"))
failedVersionIds = []
partlyFailedVersionIds = []


def sha1File(filename):
    with open(filename, 'rb') as f:
        sha1Hash = hashlib.sha1()
        while True:
            data = f.read(4096)
            if not data:
                break
            sha1Hash.update(data)
        return sha1Hash.hexdigest()


def downloadAndReplace(url, filename, destination):
    ret = requests.get(url).json()
    ret = {"minecraft": ret}

    if filename == "registries.json":
        # this is wrong in the registries (dimensions)
        ret["minecraft"]["dimension_type"] = DEFAULT_MAPPINGS["dimension_type"].copy()

    with open(destination, 'w') as file:
        json = ujson.dumps(ret)
        json = json.replace("minecraft:", "").replace(",\"default\":true", "").replace("protocol_id", "id")
        file.write(json)


def camelCaseToSnakeCase(camelCase):
    # thanks https://stackoverflow.com/questions/1175208/elegant-python-function-to-convert-camelcase-to-snake-case
    return re.sub(r'(?<!^)(?=[A-Z])', '_', camelCase).lower()


def getMinsoftEntityFieldNames(obfuscationMapLines, clazz, obfuscatedFields):
    classLineStart = -1
    for i in range(0, len(obfuscationMapLines)):
        if obfuscationMapLines[i].endswith(" -> %s:" % clazz):
            classLineStart = i
            break

    if classLineStart == -1:
        print("Could not find class (%s) in mappings" % clazz)
        exit(1)

    # find line, split by ., get last split, extract everything before the arrow
    className = obfuscationMapLines[classLineStart].split(".")[-1][:-len(" -> %s:" % clazz)]
    if VERBOSE_LOG:
        print("Found class: " + className)

    classFieldLines = []

    i = classLineStart + 1
    while True:
        if obfuscationMapLines[i].startswith("    "):
            classFieldLines.append(obfuscationMapLines[i][len("    "):])  # append and remove prefix
            i = i + 1
        else:
            break

    # print(classFieldLines)

    fields = {}
    for classLine in classFieldLines:
        if not classLine.startswith("net.minecraft.network.syncher.EntityDataAccessor "):
            continue
        classLine = classLine[len("net.minecraft.network.syncher.EntityDataAccessor "):]
        # print(classLine)
        split = classLine.split(" -> ")
        fields[split[1]] = split[0]  # obfuscatedName: fieldName

    minosoftNames = []
    for field in obfuscatedFields:
        if className not in MOJANG_MINOSOFT_FIELD_MAPPINGS:
            print("Could not find class in minosoft mappings: %s" % className)
            exit(1)
        if "data" not in MOJANG_MINOSOFT_FIELD_MAPPINGS[className] or fields[field] not in MOJANG_MINOSOFT_FIELD_MAPPINGS[className]["data"]:
            print("Could not find field in minosoft mappings: %s (class=%s)" % (className, fields[field]))
            exit(1)
        minosoftNames.append(MOJANG_MINOSOFT_FIELD_MAPPINGS[className]["data"][fields[field]])

    return className, minosoftNames


def getObfuscatedNameByClass(obfuscationMapLines, clazz):
    obfuscatedFieldNameRet = ""
    for i in range(0, len(obfuscationMapLines)):
        if re.match(".+\\.%s -> \\w{1,10}:" % clazz, obfuscationMapLines[i]):
            obfuscatedFieldNameRet = obfuscationMapLines[i].split(" -> ")[1].split(":")[0]
            break

    return obfuscatedFieldNameRet


if not os.path.isdir(DATA_FOLDER):
    os.mkdir(DATA_FOLDER)
if not os.path.isdir(TEMP_FOLDER):
    os.mkdir(TEMP_FOLDER)

for version in VERSION_MANIFEST["versions"]:
    if version["id"] == PRE_FLATTENING_UPDATE_VERSION:
        break
    versionTempBaseFolder = TEMP_FOLDER + version["id"] + "/"
    resourcesJsonKey = ("mappings/%s" % version["id"])
    if resourcesJsonKey in RESOURCE_MAPPINGS_INDEX and os.path.isfile(DATA_FOLDER + RESOURCE_MAPPINGS_INDEX[resourcesJsonKey][:2] + "/" + RESOURCE_MAPPINGS_INDEX[resourcesJsonKey] + ".tar.gz"):
        print("Skipping %s" % (version["id"]))
        continue
    print()
    print("=== %s === " % version["id"])

    if not os.path.isdir(versionTempBaseFolder):
        os.mkdir(versionTempBaseFolder)

    versionJson = requests.get(version["url"]).json()

    burger = requests.get("https://pokechu22.github.io/Burger/%s.json" % version["id"]).json()[0]

    for fileName in FILES_PER_VERSION:
        if os.path.isfile(versionTempBaseFolder + fileName):
            print("Skipping %s for %s (File already exists)" % (fileName, version["id"]))
            continue

        print("DEBUG: Generating %s for %s" % (fileName, version["id"]))

        try:
            if fileName == "blocks.json":
                downloadAndReplace(DOWNLOAD_BASE_URL + version["id"] + "/" + fileName, fileName, versionTempBaseFolder + fileName)
            elif fileName == "registries.json":
                try:
                    downloadAndReplace(DOWNLOAD_BASE_URL + version["id"] + "/" + fileName, fileName, versionTempBaseFolder + fileName)
                except Exception:
                    print("Download of registries.json failed in %s failed, using burger" % (version["id"]))
                    # data not available
                    # use burger
                    registries = DEFAULT_MAPPINGS.copy()

                    # items
                    for entityIdentifier in burger["items"]["item"]:
                        registries["item"]["entries"][entityIdentifier] = {"id": burger["items"]["item"][entityIdentifier]["numeric_id"]}

                    # biomes
                    for entityIdentifier in burger["biomes"]["biome"]:
                        registries["biome"]["entries"][entityIdentifier] = {"id": burger["biomes"]["biome"][entityIdentifier]["id"]}

                    # block ids
                    for entityIdentifier in burger["blocks"]["block"]:
                        registries["block"]["entries"][entityIdentifier] = {"id": burger["blocks"]["block"][entityIdentifier]["numeric_id"]}

                    # file write
                    with open(versionTempBaseFolder + "registries.json", 'w') as file:
                        file.write(ujson.dumps({"minecraft": registries}))
            elif fileName == "entities.json":
                if "client_mappings" not in versionJson["downloads"]:
                    print("WARN: Can not generate entities.json for %s (missing deobfuscation map)" % version["id"])
                    continue
                # download obfuscation map ToDo: make this much more efficient (aka no line looping, parsing!)
                obfuscationMapLines = requests.get(versionJson["downloads"]["client_mappings"]["url"]).content.decode("utf-8").splitlines()

                # entities
                entities = {}
                classesDone = []

                # loop over all entities
                for entityIdentifier in burger["entities"]["entity"]:
                    burgerEntityData = burger["entities"]["entity"][entityIdentifier]

                    entity = {}

                    # generate (deobfuscated) className
                    entityOriginalClassName, unused = getMinsoftEntityFieldNames(obfuscationMapLines, burgerEntityData["class"], [])

                    # check if entity was already parsed
                    if entityOriginalClassName in entities:
                        # copy current entity meta data
                        entity = entities[entityOriginalClassName]

                    # check if entity is not abstract
                    if "id" in burgerEntityData:
                        entity["id"] = burgerEntityData["id"]
                        entity["height"] = burgerEntityData["height"]
                        entity["width"] = burgerEntityData["width"]

                    # loop over all metadata entries
                    for metadataEntry in burgerEntityData["metadata"]:

                        # check if meta data is inherited by another entity
                        if metadataEntry["class"] != burgerEntityData["class"]:
                            # yes it is, check if this class was already parsed
                            if metadataEntry["class"] in classesDone:
                                # yes, we don't care about this super entity anymore
                                continue

                        # check if meta data is extended by a abstract super entity
                        if "entity" in metadataEntry:
                            # yes, we don't care about this, this will be done later
                            continue

                        # check if there is anything to parse
                        if "data" not in metadataEntry:
                            # nope, continue with next meta data entry
                            continue

                        metadataObfuscatedFields = []
                        # get all meta data fields
                        for entry in metadataEntry["data"]:
                            metadataObfuscatedFields.append(entry["field"])

                        metadataEntryOriginalClassName, metadataOriginalFields = getMinsoftEntityFieldNames(obfuscationMapLines, metadataEntry["class"], metadataObfuscatedFields)

                        metaDataEntityData = {}

                        # check if the entity is the current meta data entry
                        if metadataEntry["class"] == burgerEntityData["class"]:
                            metaDataEntityData = entity
                        elif metadataEntryOriginalClassName in entities:
                            metaDataEntityData = entities[metadataEntryOriginalClassName]
                        else:
                            metaDataEntityData = {"data": metadataOriginalFields}

                        if "data" not in metaDataEntityData:
                            metaDataEntityData["data"] = metadataOriginalFields

                        if "extends" in MOJANG_MINOSOFT_FIELD_MAPPINGS[metadataEntryOriginalClassName]:
                            metaDataEntityData["extends"] = MOJANG_MINOSOFT_FIELD_MAPPINGS[metadataEntryOriginalClassName]["extends"]
                        if metadataEntry["class"] != burgerEntityData["class"] and "entity" not in burgerEntityData:
                            entities[metadataEntryOriginalClassName] = metaDataEntityData
                        classesDone.append(metadataEntry["class"])

                    # entityOriginalClassName, metadataObfuscatedFields = getMinsoftEntityFieldNames(obfuscationMapLines, burgerEntityData["class"], [])
                    if "extends" not in entity:
                        # special meta data
                        if "extends" in MOJANG_MINOSOFT_FIELD_MAPPINGS[entityOriginalClassName]:
                            entity["extends"] = MOJANG_MINOSOFT_FIELD_MAPPINGS[entityOriginalClassName]["extends"]
                    if len(entity) == 0:
                        continue
                    classesDone.append(burgerEntityData["class"])
                    if entityIdentifier.startswith("~abstract_"):
                        entities[entityOriginalClassName] = entity
                    else:
                        entities[entityIdentifier] = entity

                # burger is missing (somehow) some entities. Try to fix them
                for classNameKey in MOJANG_MINOSOFT_FIELD_MAPPINGS:
                    if classNameKey in entities or camelCaseToSnakeCase(classNameKey) in entities:
                        continue

                    # check obfuscated mappings
                    obfuscatedFieldName = getObfuscatedNameByClass(obfuscationMapLines, classNameKey)
                    if obfuscatedFieldName == "" or obfuscatedFieldName in classesDone:
                        continue

                    # if you read this point, we need to guess. Data gets (even more?) unreliable
                    if VERBOSE_LOG:
                        print("Burger does not have the following entity (static map, bad): %s (%s)" % (classNameKey, obfuscatedFieldName))

                    entities[classNameKey] = MOJANG_MINOSOFT_FIELD_MAPPINGS[classNameKey].copy()

                # fix class name to identifier extends issues
                for entity in entities:
                    if "extends" not in entities[entity]:
                        continue
                    snakeCaseName = camelCaseToSnakeCase(entities[entity]["extends"])
                    if snakeCaseName not in entities:
                        continue
                    entities[entity]["extends"] = snakeCaseName

                # save to file
                with open(versionTempBaseFolder + "entities.json", 'w') as file:
                    file.write(ujson.dumps({"minecraft": entities}))

        except Exception:
            traceback.print_exc()
            print("ERR: Could not generate %s for %s" % (fileName, version["id"]))
            continue

    # compress the data to version.tar.gz
    tar = tarfile.open(versionTempBaseFolder + version["id"] + ".tar.gz", "w:gz")
    failed = False
    for fileName in FILES_PER_VERSION:
        try:
            tar.add(versionTempBaseFolder + fileName, arcname=fileName)
        except FileNotFoundError:
            if fileName in OPTIONAL_FILES_PER_VERSION:
                print("WARN: Could not add %s to archive, skipping this file" % fileName)
                partlyFailedVersionIds.append(version["id"])
                continue
            print("FATAL: Could not add %s to archive, skipping this version" % fileName)
            failedVersionIds.append(version["id"])
            failed = True
            break
    tar.close()
    if failed:
        if os.path.isfile(versionTempBaseFolder + version["id"] + ".tar.gz"):
            os.remove(versionTempBaseFolder + version["id"] + ".tar.gz")
        continue
    # generate sha and copy file to desired location
    sha1 = sha1File(versionTempBaseFolder + version["id"] + ".tar.gz")
    if not os.path.isdir(DATA_FOLDER + sha1[:2]):
        os.mkdir(DATA_FOLDER + sha1[:2])
    os.rename(versionTempBaseFolder + version["id"] + ".tar.gz", DATA_FOLDER + sha1[:2] + "/" + sha1 + ".tar.gz")

    if resourcesJsonKey in RESOURCE_MAPPINGS_INDEX:
        # this file already has a mapping, delete it
        hashToDelete = RESOURCE_MAPPINGS_INDEX[resourcesJsonKey]
        filenameToDelete = DATA_FOLDER + hashToDelete[:2] + "/" + hashToDelete + ".tar.gz"
        if os.path.isfile(filenameToDelete):
            shutil.rmtree(filenameToDelete)

    RESOURCE_MAPPINGS_INDEX[resourcesJsonKey] = sha1
    # cleanup (delete temp folder)
    shutil.rmtree(versionTempBaseFolder)
    # dump resources index
    with open("../src/main/resources/assets/mapping/resources.json", 'w') as file:
        ujson.dump(RESOURCE_MAPPINGS_INDEX, file)

print()
print()
print("Done")
print("While generating the following versions a fatal error occurred: %s" % failedVersionIds)
print("While generating the following versions a error occurred (some features are unavailable): %s" % partlyFailedVersionIds)
