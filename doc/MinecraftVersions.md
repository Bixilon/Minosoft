# Minecraft Versions

Once mojang releases a new minecraft version, you need to add the version to the [versions.json](/src/main/resources/assets/minosoft/mapping/versions.json).

The file is a huge json object, see below:

```json
{
  "<Version id>": {
    "name": "<version Name>",
    "packets": "<version id or custom packets>",
    "protocol_id": "<protocol id>",
    "type": "*snapshot*|release|april_fool"
  }
}
```

The json key is custom one, normally the same as the protocol id (if so, you can omit the versionId field). Because Mojang needed to change the versionIds in 1.16.4-pre1 (["New network protocol scheme, with a high bit (bit 30) set for snapshots. The protocol version will increase by 1 for each snapshot, but full releases may keep the same protocol version as the previous full release in cases where the network protocols are compatible"](https://www.minecraft.net/en-us/article/minecraft-1-16-4-pre-release-1)), I needed to change the version ids. The version id will now increment for each version/snapshot, but only if the protocol id changes too. Used for sorting and even more important: Multi protocol support. The number simply is bigger if the version is newer.

## Example

```json
{
  "753": {
    "name": "1.16.3",
    "mapping": 740
  }
}
```

## Mapping

Mapping can be an int, if so the version mapping of the corresponding version will be used. If one packet is added, removed, shifted, or simply it's id changes, you need to provide the full packet mapping. The id depends on the order.

```json
{
  "packets": {
    "c2s": [
      "confirm_teleport"
    ],
    "s2c": [
      "entity_object_spawn"
    ]
  }
}
```

All packets in `packets` are by default in `PLAY` state. If you need to specify another state, use:

```json
{
  "packets": {
    "c2s": {
      "play": [
        "confirm_teleport"
      ]
    },
    "s2c": {
      "play": [
        "entity_object_spawn"
      ]
    }
  }
}
```

The packet id is index aware, the packet id is inferred by the index. All ids are mapped in code to their corresponding classes. All names/ids are not automatically generated,
it makes no sense to do so. Names are just decided on what it actually does, not how mojang, wiki.vg or anybody else calls them.

---
Note: Do not check for `protocol_id` (especially in entity data or packets), this data is not reliable (because snapshot ids are that much higher)! Use version ids.

## How to support newer protocol versions

There are quite some steps to make a new minecraft version/snapshot work:

1. Generate all pixlyzer data (this is the first step, because it is the base for the next steps). PixLyzer generates an deobfuscated jar that you will need later on.
   All mappings for the newer version should the be pushed to (gitlab.bixilon.de, github.com and gitlab.com). Minosoft will download them from there.
2. Run the `assets_properties_generator` util python script, it will create the assets index (including pixlyzer data). The generated json file is automatically put into the resources folder,
   it is shipped with minosoft.
3. Use Burger, wiki.vg or the deobfuscated jar to generate a network diff
   (may use a java decompiler and a diff tool like meld, you can also just diff the bytecode class files, the second option may be harder first, but will be easier once you are used to it)
   between the latest supported version and the version you are trying to support (probably the next snapshot).
   Packet Ids may have shifted, you manually need to put the new data to `src/main/resources/minosoft/mapping/versions.json`. Assign a new version id (that is higher than the previous one).
   Now create a new constant with that version id at `de.bixilon.minosoft.protocol.protocol.ProtocolVersions`. You will use that constant to compare 2 versions.
   Now implement all the packet changes from the previous diff (either add new packets at `DefaultPackets.kt` or adjust the packet classes accordingly. up to you, keep things organized).
   For an example take a look at commit 853d7692d1c5281f88998d5bc5df5b51a490bc07.
4. Run integration tests (it may fail on the latest version, might need to implement missing block properties) and fire up a minecraft server and join it.
5. If there are no exceptions there is a good chance that everything works
6. Commit and push :)
