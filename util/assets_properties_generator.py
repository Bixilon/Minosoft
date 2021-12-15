#  Minosoft
#  Copyright (C) 2020 Moritz Zwerger
#
#  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
#
#  This software is not affiliated with Mojang AB, the original developer of Minecraft.

import subprocess
import urllib.request

import ujson

print("Minosoft assets properties generator")

DOWNLOAD_UNTIL_VERSION = "17w45b"
SKIP_VERSIONS = ["1.16.4", "1.18"]  # same pvn as other versions
ASSETS_PROPERTIES_PATH = "../src/main/resources/assets/minosoft/mapping/assets_properties.json"
ASSETS_PROPERTIES = ujson.load(open(ASSETS_PROPERTIES_PATH))

VERSION_MANIFEST = ujson.loads(urllib.request.urlopen('https://launchermeta.mojang.com/mc/game/version_manifest.json').read().decode("utf-8"))
PIXLYZER_INDEX = ujson.loads(urllib.request.urlopen('https://gitlab.bixilon.de/bixilon/pixlyzer-data/-/raw/master/mbf_index.min.json?inline=false').read().decode("utf-8"))

SKIP_COMPILE = True


def generateJarAssets(versionId, assetsProperties):
    process = subprocess.Popen(r'mvn -e -q exec:java -Dexec.mainClass="de.bixilon.minosoft.assets.properties.version.generator.AssetsPropertiesGenerator" -Dexec.args="\"%s\" \"%s\""' % (versionId, assetsProperties["client_jar_hash"]), shell=True, cwd='../', stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    exitCode = process.wait()
    if exitCode != 0:
        print(process.stdout.read().decode('utf-8'))
        print(process.stderr.read().decode('utf-8'))
        exit(1)
        return

    assetsProperties["jar_assets_hash"] = process.stdout.read().decode('utf-8')


if not SKIP_COMPILE:
    print("Compiling minosoft...")
    compileProcess = subprocess.Popen(r'mvn compile', shell=True, cwd='../', stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    compileExitCode = compileProcess.wait()
    if compileExitCode != 0:
        print(compileProcess.stdout.read().decode('utf-8'))
        print(compileProcess.stderr.read().decode('utf-8'))
    print("Minosoft compiled!")


def generateVersion(version):
    if version["id"] in SKIP_VERSIONS:
        print("Force skipping %s" % version["id"])
        return
    changed = False

    assetsProperties = {}
    if version["id"] in ASSETS_PROPERTIES:
        assetsProperties = ASSETS_PROPERTIES[version["id"]]

    ASSETS_PROPERTIES[version["id"]] = assetsProperties

    if "pixlyzer_hash" not in assetsProperties or assetsProperties["pixlyzer_hash"] != PIXLYZER_INDEX[version["id"]]:
        if version["id"] in PIXLYZER_INDEX:
            assetsProperties["pixlyzer_hash"] = PIXLYZER_INDEX[version["id"]]
            changed = True
        else:
            print("PixLyzer does not support %s. This version probably won't work in Minosoft" % version["id"])

    versionJson = ujson.loads(urllib.request.urlopen(version["url"]).read().decode("utf-8"))
    if "index_version" in assetsProperties and "jar_assets_hash" in assetsProperties:
        return changed

    assetsProperties["index_version"] = versionJson["assetIndex"]["id"]
    assetsProperties["index_hash"] = versionJson["assetIndex"]["sha1"]
    assetsProperties["client_jar_hash"] = versionJson["downloads"]["client"]["sha1"]

    if "jar_assets_hash" not in assetsProperties:
        try:
            generateJarAssets(version["id"], assetsProperties)
            changed = True
        except Exception:
            failedVersionIds.append(version["id"])
            print("%s failed!" % version["id"])
            return

    return changed


for version in VERSION_MANIFEST["versions"]:
    if version["id"] == DOWNLOAD_UNTIL_VERSION:
        break
    save = generateVersion(version)
    if save:
        print("Generated %s" % version["id"])
        with open(ASSETS_PROPERTIES_PATH, 'w') as file:
            ujson.dump(ASSETS_PROPERTIES, file)
    else:
        print("Skipped %s" % version["id"])

print()
print()
print("Done")
