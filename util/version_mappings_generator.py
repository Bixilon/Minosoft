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

import os
import subprocess
import urllib.request

import ujson

print("Minecraft mappings downloader (and generator)")

DOWNLOAD_UNTIL_VERSION = "18w01a"
SKIP_VERSIONS = ["1.13-pre6", "1.13-pre5"]
DATA_FOLDER = "../data/resources/"
TEMP_FOLDER = DATA_FOLDER + "tmp/"
DOWNLOAD_BASE_URL = "https://apimon.de/mcdata/"
RESOURCE_MAPPINGS_INDEX = ujson.load(open("../src/main/resources/assets/mapping/resources.json"))
VERSION_MANIFEST = ujson.loads(urllib.request.urlopen('https://launchermeta.mojang.com/mc/game/version_manifest.json').read().decode("utf-8"))
VERBOSE_LOG = False
failedVersionIds = []
partlyFailedVersionIds = []


if not os.path.isdir(DATA_FOLDER):
    os.mkdir(DATA_FOLDER)
if not os.path.isdir(TEMP_FOLDER):
    os.mkdir(TEMP_FOLDER)


# compile minosoft


def generateJarAssets(versionId):
    print("Generating jar asset hash: %s" % versionId)
    generateProcess = ""
    try:
        generateProcess = subprocess.run(r'mvn exec:java -Dexec.mainClass="de.bixilon.minosoft.generator.JarHashGenerator" -Dexec.args="\"%s\""' % versionId, cwd=r'../', shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        # reload mappings
        global RESOURCE_MAPPINGS_INDEX
        RESOURCE_MAPPINGS_INDEX = ujson.load(open("../src/main/resources/assets/mapping/resources.json"))
    except Exception:
        print(generateProcess.stdout)
        print(generateProcess.stderr)


print("Compiling minosoft...")
compileProcess = ""
try:
    compileProcess = subprocess.run(r'mvn compile', shell=True, check=True, cwd='../', stdout=subprocess.PIPE, stderr=subprocess.PIPE)
except Exception:
    print(compileProcess.stdout)
    print(compileProcess.stderr)
    exit(1)
print("Minosoft compiled!")


def downloadVersion(version):
    resourcesVersion = {}
    if version["id"] in SKIP_VERSIONS:
        print("Force skipping %s" % version["id"])
        return

    if version["id"] in RESOURCE_MAPPINGS_INDEX["versions"]:
        print("Skipping %s" % version["id"])
    print()

    print("DEBUG: Downloading versions json for %s" % version["id"])
    versionJson = ujson.loads(urllib.request.urlopen(version["url"]).read().decode("utf-8"))

    if "index_version" not in resourcesVersion:
        resourcesVersion["index_version"] = versionJson["assetIndex"]["id"]
        resourcesVersion["index_hash"] = versionJson["assetIndex"]["sha1"]
        resourcesVersion["client_jar_hash"] = versionJson["downloads"]["client"]["sha1"]
        RESOURCE_MAPPINGS_INDEX["versions"][version["id"]] = resourcesVersion
        # dump resources index
        with open("../src/main/resources/assets/mapping/resources.json", 'w') as file:
            ujson.dump(RESOURCE_MAPPINGS_INDEX, file)
        # start jar hash generator
        try:
            generateJarAssets(version["id"])
        except Exception:
            failedVersionIds.append(version["id"])
            return

    RESOURCE_MAPPINGS_INDEX["versions"][version["id"]] = resourcesVersion
    # dump resources index
    with open("../src/main/resources/assets/mapping/resources.json", 'w') as file:
        ujson.dump(RESOURCE_MAPPINGS_INDEX, file)


for version in VERSION_MANIFEST["versions"]:
    if version["id"] == DOWNLOAD_UNTIL_VERSION:
        break
    downloadVersion(version)

print()
print()
print("Done")
print("While generating the following versions a fatal error occurred: %s" % failedVersionIds)
print("While generating the following versions a error occurred (some features are unavailable): %s" % partlyFailedVersionIds)
