#  Minosoft
#  Copyright (C) 2020 Lukas Eisenhauer
#
#  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
#
#  This software is not affiliated with Mojang AB, the original developer of Minecraft.

import io
import sys
import urllib.request
import zipfile

import ujson

if len(sys.argv) != 3:
    print("Usage: %s <destination path> <jar url>" % sys.argv[0])
    exit(1)

blockStates = {}
blockModels = {}

modName = "minecraft"

print("Downloading minecraft jar...")

# thanks: https://stackoverflow.com/questions/60171502/requests-get-is-very-slow

response = urllib.request.urlopen(sys.argv[2])
print("Unpacking minecraft jar...")

zip = zipfile.ZipFile(io.BytesIO(response.read()), "r")

files = zip.namelist()

print("Loading blockstates...")


def readRotations(apply, current):
    if "x" in current:
        apply["x"] = current["x"]
    if "y" in current:
        apply["y"] = current["y"]
    if "z" in current:
        apply["z"] = current["z"]


def readPart(part):
    properties = []
    if "when" in part:
        when = part["when"]
        if "OR" in when:
            for item in when["OR"]:
                properties.append(item)
        else:
            properties.append(part["when"])
    apply = {}
    current = part["apply"]
    if type(current) == type([]):
        current = current[0]
    if "/" in current["model"]:
        apply["model"] = current["model"].split("/")[1]
    else:
        apply["model"] = current["model"]
    readRotations(apply, current)
    result = []
    for item in properties:
        state = {"properties": item}
        for i in apply:
            state[i] = apply[i]
        result.append(state)
    if len(result) == 0:
        result.append(apply)
    return result


for blockStateFile in [f for f in files if f.startswith('assets/minecraft/blockstates/')]:
    with zip.open(blockStateFile) as file:
        tempData = file.read().decode("utf-8")
        if tempData.endswith("n"):
            # why the hell are mojangs json files incorrect?
            # in 19w02a (https://launcher.mojang.com/v1/objects/8664f5d1b428d5ba8a936ab9c097cc78821d06e6/client.jar) the json ends with a random "n"
            tempData = tempData[:-1]
        data = ujson.loads(tempData)
        block = {}
        if "variants" in data:
            variants = data["variants"]
            states = []
            for variant in variants:
                state = {}
                properties = {}
                if variant != "" and variant != "normal" and variant != "map" and variant != "all":
                    for part in variant.split(","):
                        properties[part.split("=")[0]] = part.split("=")[1]
                state["properties"] = properties
                current = variants[variant]
                if type(current) == type([]):
                    current = current[0]

                if "/" in current["model"]:
                    state["model"] = current["model"].split("/")[1]
                else:
                    state["model"] = current["model"]
                readRotations(state, current)
                states.append(state)
            block = {
                "states": states
            }
        elif "multipart" in data:
            parts = data["multipart"]
            conditional = []
            for part in parts:
                conditional.extend(readPart(part))
            block = {
                "conditional": conditional
            }
    blockStates[blockStateFile.split(".")[0].split("/")[-1]] = block

print("Loading models...")
for blockModelFile in [f for f in files if f.startswith('assets/minecraft/models/block/')]:
    with zip.open(blockModelFile) as file:
        data = ujson.load(file)
    blockModels[blockModelFile.split(".")[0].split("/")[-1]] = data

print("Combining files...")
finalJson = {
    modName: {
        "blockStates": blockStates,
        "blockModels": blockModels
    }
}

print("Saving...")
with open(sys.argv[1], "w+") as file:
    finalJson = ujson.dumps(finalJson)
    file.write(finalJson.replace("minecraft:", ""))

print("Finished successfully")
