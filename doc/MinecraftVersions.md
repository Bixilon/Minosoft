# Minecraft Versions

Once mojang releases a new minecraft version, you need to add the version to the [versions.json](/src/main/resources/assets/minosoft/mapping/versions.json).

The file is a huge json object, see below:

```json
{
  "<Version id>": {
    "name": "<Version Name>",
    "mapping": "<Version id or custom mapping>",
    "protocol_id": "<Protocol id>"
  }
}
```

The json key is custom one, normally the same as the protocol id (if so, you can omit the versionId field). Because Mojang needed to change the versionIds in
1.16.4-pre1 (["New network protocol scheme, with a high bit (bit 30) set for snapshots. The protocol version will increase by 1 for each snapshot, but full releases may keep the same protocol version as the previous full release in cases where the network protocols are compatible"](https://www.minecraft.net/en-us/article/minecraft-1-16-4-pre-release-1)), I needed to change the version Ids. The version id will now increment for each version/snapshot, but only if the protocol id changes too. Used
for sorting and even more important: Multi protocol support. The number simply is bigger if the version is newer.

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
  "mapping": {
    "c2s": [
      "PLAY_TELEPORT_CONFIRM"
    ],
    "s2c": [
      "PLAY_ENTITY_OBJECT_SPAWN"
    ]
  }
}
```

---
Note: Do not check for `protocol_id` (especially in EntityMetaData or Packets), this data is not reliable (because snapshot ids are that much higher)! Use version Ids.
