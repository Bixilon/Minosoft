"""
* Codename Minosoft
* Copyright (C) 2020 Moritz Zwerger
*
* This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
*
*  This software is not affiliated with Mojang AB, the original developer of Minecraft.
"""
import os
import requests
import shutil
import subprocess
import sys

javaPath = "/usr/lib/jvm/java-8-openjdk-amd64/bin/java"
print("Minecraft server wrapper")
while True:
    version = input('Enter a minecraft version: ')
    if len(version) < 2 or len(version) > 10 or not version.find("/"):
        print("Invalid version!")
        continue
    if not os.path.isfile("./" + version + "/server.jar"):
        # download
        manifest = requests.get('https://launchermeta.mojang.com/mc/game/version_manifest.json').json()
        versionJson = ""
        for key in manifest["versions"]:
            if key["id"] == version:
                versionJson = key["url"]
                break
        if versionJson == "":
            print("Snapshot not found!")
            continue
        versionJson = requests.get(versionJson).json()
        server = versionJson["downloads"]["server"]["url"]
        if server == "":
            print("Invalid server jar url!")
            continue

        print("Copying template...", end="")
        shutil.copytree("TEMPLATE", version)
        print("done")

        print("Downloading server.jar...", end="")
        server = requests.get(server)
        with open("./" + version + '/server.jar', 'wb') as f:
            f.write(server.content)
        print("done")

    print("Starting server...", end="")

    process = ""
    try:
        process = subprocess.Popen(javaPath + " -jar server.jar nogui",
                                   shell=True, stdout=subprocess.PIPE, cwd=version, universal_newlines=True,
                                   stderr=subprocess.STDOUT)

        while True:
            nextline = process.stdout.readline()
            if nextline == '' and process.poll() is not None:
                break
            sys.stdout.write(nextline)
            sys.stdout.flush()
        process.wait()

    except KeyboardInterrupt:
        print("CTRL + C")
        process.kill()

    print("Server stopped (" + str(process.returncode) + ")")
