# Various tools to make the development easier

## Server Wrapper

The script asks you for a version of minecraft, you enter it (can be a snapshot), The script copies the TEMPLATE folder and starts the server.jar. If the server.jar does not exist, it will download the correct file from mojangs servers.

#### Installation / Usage

1. Download the script
2. Create a folder called "TEMPLATE" with the lowest version possible (if you use 1.8 - 1.16 inclusive snapshots, create a world in 1.7) with your map (minecraft can upgrade a world, but it cannot downgrade versions, especially not after 1.13). It should contain an eula.txt, your ops.json, your world, and all files you need.
3. Adjust the `javaPath` in the file (edit with an editor). If Java 8 is your default JRE, just insert `java`.
4. Run the script with `python3 server_wrapper.py`. You can pass the parameter `download-all` and it will create an offline cache for all minecraft versions (latest - first)

## Mappings Generator

This data generates the jar assets hash for every version

#### Installation / Usage

1. Download the script
2. Run the script with `python3 version_mappings_generator.py` and wait for the process to complete. Currently, some versions will fail, because it is difficult to generate the blocks.json. Feel free to make a Pull Request :): ['1.13-pre6', '1.13-pre5', '17w50a', '17w49b', '17w49a', '17w48a', '17w47b', '17w47a']
