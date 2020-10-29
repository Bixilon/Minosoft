#  Minosoft
#  Copyright (C) 2020 Moritz Zwerger
#
#  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#
#   This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
#
#   This software is not affiliated with Mojang AB, the original developer of Minecraft.
#
#  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#
#   This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
#
#   This software is not affiliated with Mojang AB, the original developer of Minecraft.

import \
    os
import \
    requests
import \
    shutil
import \
    tarfile
import \
    traceback
import \
    ujson

print(
    "Minecraft mappings downloader (and generator)")

PRE_FLATTENING_UPDATE_VERSION = "17w46a"
DATA_FOLDER = "./mcdata/"
FILES_PER_VERSION = [
    "blocks.json",
    "registries.json"]
DOWNLOAD_BASE_URL = "https://apimon.de/mcdata/"
manifest = requests.get(
    'https://launchermeta.mojang.com/mc/game/version_manifest.json').json()
failed = []
defaultMappings = ujson.load(
    open(
        "mappingsDefaults.json"))

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
                reformatted = requests.get(DOWNLOAD_BASE_URL + version["id"] + "/" + fileName).json()
                reformatted = {"minecraft": reformatted}
                if fileName == "registries.json":
                    # this is wrong in the registries (dimensions)
                    reformatted["minecraft"]["dimension_type"] = defaultMappings["dimension_type"].copy()
                with open(versionBaseFolder + fileName, 'w') as file:
                    json = ujson.dumps(reformatted)
                    json = json.replace("minecraft:", "").replace(",\"default\":true", "").replace("protocol_id", "id")
                    file.write(json)
            except Exception:
                try:
                    print("Download of %s failed in %s, using burger" % (fileName, version["id"]))
                    burger = requests.get("https://pokechu22.github.io/Burger/%s.json" % version["id"]).json()[0]
                    # data not available
                    # use burger
                    registries = defaultMappings.copy()

                    # items
                    for key in burger["items"]["item"]:
                        registries["item"]["entries"][key] = {"id": burger["items"]["item"][key]["numeric_id"]}

                    # entities
                    for key in burger["entities"]["entity"]:
                        if key.startswith("~abstract_"):
                            continue
                        registries["entity_type"]["entries"][key] = {"id": burger["entities"]["entity"][key]["id"]}

                    # biome
                    for key in burger["biomes"]["biome"]:
                        registries["biome"]["entries"][key] = {"id": burger["biomes"]["biome"][key]["id"]}

                    for key in burger["blocks"]["block"]:
                        registries["block"]["entries"][key] = {"id": burger["blocks"]["block"][key]["numeric_id"]}

                    # file write
                    with open(versionBaseFolder + "registries.json", 'w') as file:
                        file.write(ujson.dumps({"minecraft": registries}))

                    if fileName == "blocks.json":
                        # more missing....
                        raise Exception("blocks.json is missing")

                except Exception:
                    traceback.print_exc()
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
