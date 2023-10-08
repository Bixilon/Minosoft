#  Minosoft
#  Copyright (C) 2020-2023 Moritz Zwerger
#
#  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
#
#  This software is not affiliated with Mojang AB, the original developer of Minecraft.

import subprocess
import ujson
import urllib.request

print("Minosoft assets properties generator")

DOWNLOAD_UNTIL_VERSION = "17w45b"
SKIP_VERSIONS = ["1.16.4", "1.18"]  # same pvn as other versions
ASSETS_PROPERTIES_PATH = "../src/main/resources/assets/minosoft/mapping/assets_properties.json"
ASSETS_PROPERTIES = ujson.load(open(ASSETS_PROPERTIES_PATH))

VERSION_MANIFEST = ujson.loads(urllib.request.urlopen('https://launchermeta.mojang.com/mc/game/version_manifest.json').read().decode("utf-8"))
PIXLYZER_INDEX = ujson.loads(urllib.request.urlopen('https://gitlab.bixilon.de/bixilon/pixlyzer-data/-/raw/master/mbf_index.min.json?inline=false').read().decode("utf-8"))


def generate_jar_assets(version_id, assets_properties):
    process = subprocess.Popen(r'./gradlew -q assetsProperties --args="\"%s\" \"%s\""' % (version_id, assets_properties["client_jar_hash"]), shell=True, cwd='../', stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    exit_code = process.wait()
    if exit_code != 0:
        print(process.stdout.read().decode('utf-8'))
        print(process.stderr.read().decode('utf-8'))
        exit(1)
        return

    (hash, tar_bytes) = process.stdout.read().decode('utf-8').split(":")
    assets_properties["jar_assets_hash"] = hash
    assets_properties["jar_assets_tar_bytes"] = tar_bytes


def generate_version(version):
    if version["id"] in SKIP_VERSIONS:
        print("Force skipping %s" % version["id"])
        return
    changed = False

    assets_properties = {}
    if version["id"] in ASSETS_PROPERTIES:
        assets_properties = ASSETS_PROPERTIES[version["id"]]

    ASSETS_PROPERTIES[version["id"]] = assets_properties

    if "pixlyzer_hash" not in assets_properties or assets_properties["pixlyzer_hash"] != PIXLYZER_INDEX[version["id"]]:
        if version["id"] in PIXLYZER_INDEX:
            assets_properties["pixlyzer_hash"] = PIXLYZER_INDEX[version["id"]]
            changed = True
        else:
            print("PixLyzer does not support %s. This version probably won't work in Minosoft" % version["id"])

    version_json = ujson.loads(urllib.request.urlopen(version["url"]).read().decode("utf-8"))
    if "index_version" in assets_properties and "jar_assets_hash" in assets_properties:
        return changed

    assets_properties["index_version"] = version_json["assetIndex"]["id"]
    assets_properties["index_hash"] = version_json["assetIndex"]["sha1"]
    assets_properties["client_jar_hash"] = version_json["downloads"]["client"]["sha1"]

    if "jar_assets_hash" not in assets_properties:
        try:
            generate_jar_assets(version["id"], assets_properties)
            changed = True
        except Exception:
            print("%s failed!" % version["id"])
            return

    return changed


def main():
    for version in VERSION_MANIFEST["versions"]:
        if version["id"] == DOWNLOAD_UNTIL_VERSION:
            break
        save = generate_version(version)
        if save:
            print("Generated %s" % version["id"])
            with open(ASSETS_PROPERTIES_PATH, 'w') as file:
                ujson.dump(ASSETS_PROPERTIES, file)
        else:
            print("Skipped %s" % version["id"])
    print()
    print()
    print("Done")


if __name__ == '__main__':
    main()
