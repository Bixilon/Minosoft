#  Codename Minosoft
#  Copyright (C) 2020 Moritz Zwerger
#
#  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#
#   This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
#
#   This software is not affiliated with Mojang AB, the original developer of Minecraft.

import os
import requests
import shutil
import tarfile
import ujson

print("Minecraft mappings downloader (and generator)")

PRE_FLATTENING_UPDATE_VERSION = "17w46a"
DATA_FOLDER = "./mcdata/"
FILES_PER_VERSION = ["blocks.json", "registries.json"]
DOWNLOAD_BASE_URL = "https://apimon.de/mcdata/"
manifest = requests.get('https://launchermeta.mojang.com/mc/game/version_manifest.json').json()
failed = []


def retMinified(url):
    blocks = requests.get(url).json()
    blocksReformatted = {}
    for block in blocks:
        blocksReformatted[block.replace("minecraft:", "")] = blocks[block]
    return {"minecraft": blocksReformatted}


if not os.path.isdir(DATA_FOLDER):
    os.mkdir(DATA_FOLDER)

for version in manifest["versions"]:
    if version["id"] == PRE_FLATTENING_UPDATE_VERSION:
        break
    versionBaseFolder = DATA_FOLDER + version["id"] + "/"
    if os.path.isfile(DATA_FOLDER + version["id"] + ".tar.gz"):
        print("Skipping %s" % (version["id"]))
        continue
    if not os.path.isdir(versionBaseFolder):
        os.mkdir(versionBaseFolder)
    for fileName in FILES_PER_VERSION:
        if not os.path.isfile(versionBaseFolder + fileName):
            print("Downloading %s for %s" % (fileName, version["id"]))
            try:
                reformatted = retMinified(DOWNLOAD_BASE_URL + version["id"] + "/" + fileName)
                with open(versionBaseFolder + fileName, 'w') as file:
                    json = ujson.dumps(reformatted)
                    json = json.replace("minecraft:", "").replace(",\"default\":true", "")
                    file.write(json)
            except Exception:
                failed.append(version["id"])
                print("Could not download mappings for %s in %s" % (version["id"], fileName))
        else:
            print("Skipping %s for %s" % (fileName, version["id"]))
    if not version["id"] in failed:
        # compress the data to version.tar.gz
        tar = tarfile.open(DATA_FOLDER + version["id"] + ".tar.gz", "w:gz")
        for fileName in FILES_PER_VERSION:
            tar.add(versionBaseFolder + fileName, arcname=fileName)
        tar.close()
        shutil.rmtree(versionBaseFolder)

print("Done, %s failed" % failed)
