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
import requests
import shutil
import tarfile
import traceback
import ujson

print("Minecraft mappings downloader (and generator)")

PRE_FLATTENING_UPDATE_VERSION = "17w46a"
DATA_FOLDER = "../data/resources/"
TEMP_FOLDER = DATA_FOLDER + "tmp/"
FILES_PER_VERSION = ["blocks.json", "registries.json"]
DOWNLOAD_BASE_URL = "https://apimon.de/mcdata/"
manifest = requests.get('https://launchermeta.mojang.com/mc/game/version_manifest.json').json()
failedVersionIds = []
defaultMappings = ujson.load(open("mappingsDefaults.json"))
resourceMappingsIndex = ujson.load(open("../src/main/resources/assets/mapping/resources.json"))


def sha1File(filename):
    with open(filename, 'rb') as f:
        sha1 = hashlib.sha1()
        while True:
            data = f.read(4096)
            if not data:
                break
            sha1.update(data)
    return sha1.hexdigest()


def downloadAndReplace(url, filename, destination):
    ret = requests.get(url).json()
    ret = {"minecraft": ret}

    if fileName == "registries.json":
        # this is wrong in the registries (dimensions)
        ret["minecraft"]["dimension_type"] = defaultMappings["dimension_type"].copy()

    with open(destination, 'w') as file:
        json = ujson.dumps(ret)
        json = json.replace("minecraft:", "").replace(",\"default\":true", "").replace("protocol_id", "id")
        file.write(json)


if not os.path.isdir(DATA_FOLDER):
    os.mkdir(DATA_FOLDER)
if not os.path.isdir(TEMP_FOLDER):
    os.mkdir(TEMP_FOLDER)

for version in manifest["versions"]:
    if version["id"] == PRE_FLATTENING_UPDATE_VERSION:
        break
    versionTempBaseFolder = TEMP_FOLDER + version["id"] + "/"
    resourcesJsonKey = ("mappings/%s" % version["id"])
    if resourcesJsonKey in resourceMappingsIndex and os.path.isfile(DATA_FOLDER + resourceMappingsIndex[resourcesJsonKey][:2] + "/" + resourceMappingsIndex[resourcesJsonKey] + ".tar.gz"):
        print("Skipping %s" % (version["id"]))
        continue

    if not os.path.isdir(versionTempBaseFolder):
        os.mkdir(versionTempBaseFolder)

    burger = requests.get("https://pokechu22.github.io/Burger/%s.json" % version["id"]).json()[0]

    for fileName in FILES_PER_VERSION:
        if os.path.isfile(versionTempBaseFolder + fileName):
            print("Skipping %s for %s (File already exists)" % (fileName, version["id"]))
            continue

        print("Generating %s for %s" % (fileName, version["id"]))

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
                    registries = defaultMappings.copy()

                    # items
                    for key in burger["items"]["item"]:
                        registries["item"]["entries"][key] = {"id": burger["items"]["item"][key]["numeric_id"]}

                    # biomes
                    for key in burger["biomes"]["biome"]:
                        registries["biome"]["entries"][key] = {"id": burger["biomes"]["biome"][key]["id"]}

                    # block ids
                    for key in burger["blocks"]["block"]:
                        registries["block"]["entries"][key] = {"id": burger["blocks"]["block"][key]["numeric_id"]}

                    # file write
                    with open(versionTempBaseFolder + "registries.json", 'w') as file:
                        file.write(ujson.dumps({"minecraft": registries}))
        except Exception:
            traceback.print_exc()
            failedVersionIds.append(version["id"])
            print("Could not generate mappings for %s in %s" % (version["id"], fileName))
            continue

    # compress the data to version.tar.gz
    tar = tarfile.open(versionTempBaseFolder + version["id"] + ".tar.gz", "w:gz")
    for fileName in FILES_PER_VERSION:
        tar.add(versionTempBaseFolder + fileName, arcname=fileName)
    tar.close()
    # generate sha and copy file to desired location
    sha1 = sha1File(versionTempBaseFolder + version["id"] + ".tar.gz")
    if not os.path.isdir(DATA_FOLDER + sha1[:2]):
        os.mkdir(DATA_FOLDER + sha1[:2])
    os.rename(versionTempBaseFolder + version["id"] + ".tar.gz", DATA_FOLDER + sha1[:2] + "/" + sha1 + ".tar.gz")

    if resourcesJsonKey in resourceMappingsIndex:
        # this file already has a mapping, delete it
        hashToDelete = resourceMappingsIndex[resourcesJsonKey]
        shutil.rmtree(DATA_FOLDER + hashToDelete[:2] + "/" + hashToDelete + ".tar.gz")

    resourceMappingsIndex[resourcesJsonKey] = sha1
    # cleanup (delete temp folder)
    shutil.rmtree(versionTempBaseFolder)
    # dump resources index
    with open("../src/main/resources/assets/mapping/resources.json", 'w') as file:
        ujson.dump(resourceMappingsIndex, file)

print("Done, %s failed" % failedVersionIds)
