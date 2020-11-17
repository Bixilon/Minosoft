# Various tools to make the development easier

## Server Wrapper
The script asks you for a version of minecraft, you enter it (can be a snapshot), The script copies the TEMPLATE folder and starts the server.jar. If the server.jar does not exist, it will download the correct file from mojangs servers.
#### Installation / Usage
1. Download the script
2. Create a folder called "TEMPLATE" with the lowest version possible (if you use 1.8 - 1.16 inclusive snapshots, create a world in 1.7) with your map (minecraft can upgrade a world, but it cannot downgrade versions, especially not after 1.13). It should contain an eula.txt, your ops.json, your world, and all files you need.
3. Adjust the `javaPath` in the file (edit with an editor). If Java 8 is your default JRE, just insert `java`.
4. Run the script with `python3 server_wrapper.py`. You can pass the parameter `download-all` and it will create an offline cache for all minecraft versions (latest - first)

## Mappings Generator
This script creates a modified version of the mappings after the flattening update (1.13). See [Data Generators for more details](https://wiki.vg/Data_Generators).
The problem is, that there are some versions that have been "flattened", but there are not data generators or that the old format is getting used (the items.json and not the registries.json).
The script downloads all files from https://apimon.de/mcdata. If the download (of `registries.json`) fails, the script ties do download and create the format based on [Burger](https://pokechu22.github.io/Burger/).
The `entities.json` will be generated from burger, the original deobfuscation maps from mojang and some static mapping.
#### Installation / Usage
1. Download the script
2. Run the script with `python3 version_mappings_generator.py` and wait for the process to complete. Currently, some versions will fail, because it is difficult to generate the blocks.json. Feel free to make a Pull Request :): ['1.13-pre6', '1.13-pre5', '17w50a', '17w49b', '17w49a', '17w48a', '17w47b', '17w47a']


## Current output (`20w46a` is the latest version at this time)
```
Done
While generating the following versions a fatal error occurred: ['1.15.2-pre2', '1.13-pre6', '1.13-pre5', '17w50a', '17w49b', '17w49a', '17w48a', '17w47b', '17w47a']
While generating the following versions a error occurred (some features are unavailable): ['19w35a', '19w34a', '1.14.4-pre7', '1.14.4-pre6', '1.14.4-pre5', '1.14.4-pre4', '1.14.4-pre3', '1.14.4-pre2', '1.14.4-pre1', '1.14.3', '1.14.3-pre4', '1.14.3-pre3', '1.14.3-pre2', '1.14.3-pre1', '1.14.2', '1.14.2 Pre-Release 4', '1.14.2 Pre-Release 3', '1.14.2 Pre-Release 2', '1.14.2 Pre-Release 1', '1.14.1', '1.14.1 Pre-Release 2', '1.14.1 Pre-Release 1', '1.14', '1.14 Pre-Release 5', '1.14 Pre-Release 4', '1.14 Pre-Release 3', '1.14 Pre-Release 2', '1.14 Pre-Release 1', '19w14b', '19w14a', '3D Shareware v1.34', '19w13b', '19w13a', '19w12b', '19w12a', '19w11b', '19w11a', '19w09a', '19w08b', '19w08a', '19w07a', '19w06a', '19w05a', '19w04b', '19w04a', '19w03c', '19w03b', '19w03a', '19w02a', '18w50a', '18w49a', '18w48b', '18w48a', '18w47b', '18w47a', '18w46a', '18w45a', '18w44a', '18w43c', '18w43b', '18w43a', '1.13.2', '1.13.2-pre2', '1.13.2-pre1', '1.13.1', '1.13.1-pre2', '1.13.1-pre1', '18w33a', '18w32a', '18w31a', '18w30b', '18w30a', '1.13', '1.13-pre10', '1.13-pre9', '1.13-pre8', '1.13-pre7', '1.13-pre4', '1.13-pre3', '1.13-pre2', '1.13-pre1', '18w22c', '18w22b', '18w22a', '18w21b', '18w21a', '18w20c', '18w20b', '18w20a', '18w19b', '18w19a', '18w16a', '18w15a', '18w14b', '18w14a', '18w11a', '18w10d', '18w10c', '18w10b', '18w10a', '18w09a', '18w08b', '18w08a', '18w07c', '18w07b', '18w07a', '18w06a', '18w05a', '18w03b', '18w03a', '18w02a', '18w01a']

```
