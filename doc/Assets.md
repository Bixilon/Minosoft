# Minecraft Assets

Assets are [Game Files](https://wiki.vg/Game_files). For example:
 - Textures
 - Sounds
 - Language Files
 - (Icons)
 - (Block models)
 - ...
 
 There are core assets (essential for the game), delivered in the `client.jar` (Textures, en_US language, models, etc)
 and there are "other assets", like sounds, all other languages, available on `https://resources.download.minecraft.net/`.
 
 Minosoft downloads the current best available (hardcoded) client.jar and extracts the `minecraft` folder in it into a temporary one.
 Now the (sha1) hash is getting calculated and then the file will be gzipped and saved into `%Dir%/assets/objects/<first 2 digs of hash>/<full hash>.gz`.
 Next, a file called index.json will be created. It is a simple mapping: file name -> hash and looks like this:
 ```json
{
  "minecraft/lang/de_de.json": "a8f1b2babf63e3fa21d24728271eaef0d8d33041"
}
```
Next, in the `version.json` ([Example 1.16.4-pre1](https://launchermeta.mojang.com/v1/packages/edcca0531de05c4b15007ca689b575a33b9d96a2/1.16.4-pre1.json)) there is a sub json object called `assetIndex`.
The asset index will be downloaded and stored. The hash of the file is hardcoded into minosoft to ensure that the assets are compatible. Next we loop over all elements and check if the file exists.
If not, we will download it and store it gzip compressed.

## Valid (Relevant) files
Before downloading a file, the file is checked for relevance. Relevant files are prefixed with the following strings (or the file path):

- `minecraft/lang/` -> Language files
- `minecraft/sounds.json` -> Sound meta data and index file
- `minecraft/sounds/` -> Sounds
 - `minecraft/textures/` -> Textures
 - `minecraft/font/` -> Fonts

## Modifications
If you want to edit an existing file, you should disable `verify-all-assets` (in `debug`), otherwise your changes will be recognised as corruption and will be overwritten.
