# Various tools to make the development easier

## serverWrapper
The script asks you for a version of minecraft, you enter it (can be a snapshot), The script copies the TEMPLATE folder and starts the server.jar. If the server.jar does not exist, it will download the correct file from mojangs servers.
#### Installation / Usage
1. Download the script
2. Create a folder called "TEMPLATE" with the lowest version possible (if you use 1.8 - 1.16 inclusive snapshots, create a world in 1.7) with your map (minecraft can upgrade a world, but it cannot downgrade versions, especially not after 1.13). It should contain an eula.txt, your ops.json, your world, and all files you need.
3. Run the script with `python3 serverWrapper.py`. You can pass the parameter `download-all` and it will create an offline cache for all minecraft versions (latest - first)

## mappingsDownloader
This script creates a modified version of the mappings after the flattening update (1.13). See [Data Generators for more details](https://wiki.vg/Data_Generators).
The problem is, that there are some versions that have been "flattened", but there are not data generators or that the old format is getting used (the items.json and not the registries.json).
The script downloads all files from https://apimon.de/mcdata. If the download fails, the script ties do download and create the format based on [Burger](https://pokechu22.github.io/Burger/).
This is currently work in progress, it can only generate the registries.json, not the `blocks.json`.
#### Installation / Usage
1. Download the script
2. Run the script with `python3 mappingsDownloader.py` and wait for the process to complete. Currently, some versions will fail, because it is difficult to generate the blocks.json. Feel free to make a Pull Request :): ['1.13-pre6', '1.13-pre5', '17w50a', '17w49b', '17w49a', '17w48a', '17w47b', '17w47a']
