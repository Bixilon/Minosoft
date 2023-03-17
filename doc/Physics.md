## Check (removed)

getPrimaryPassenger
fov multiplier
collision detector
critical (attack)

# Physics

Matching physics are actually one of the most essential part of this project (because of anti cheat).
They should actually match exactly (every bit, with all float precision loss).
I am doing the best to make to maintain it, but the minecraft physics engine is a [...] nightmare.
I **can not and will never guarantee** a 100% match (i.e. eventual anti cheat ban).

## Improving them

So if you get banned (or see that you get flagged or lagged back because of minosoft),
please don't blame me and instead open an issue asap. Provide as many details as possible and try to make a guess why it
happened.
I'll add a pixlyzer extractor and investigate the issue asap and provide a fix.

## Tests

I wrote a lot of tests (500+) that try to make it correct and preventing anti cheat bans.
The data source is a pixlyzer module, but it is not public at the moment. I might publish the module with the data in
the future.

## General

The whole physics engine is based of [22w45a](https://www.minecraft.net/en-us/article/minecraft-snapshot-22w45a).
That was simply the newest version when I started rewriting the engine.

## Supported mechanisms

| Feature         | State | Comment                         |
|-----------------|-------|---------------------------------|
| Basics          | ✅     | walking, jumping, sneaking, ... |
| Elytra          | ✅     | rocket boosting not working     |
| Swimming        | ✅     | 1.13                            |
| Scaffolding     | ❌     | 1.14                            |
| Trident         | ❌     | riptide attack                  |
| Piston          | ❌     | entity pushing                  |
| Shulker box     | ❌     | opening collision box           |
| Riding          | ❌     |                                 |
| Entity cramming | ❌     | colliding with other entities   |
| powder snow     | ❌     | kind of broken                  |

## Notable version changes

Source: https://www.mcpk.wiki/wiki/Version_Differences

- https://bugs.mojang.com/browse/MC-135831
- 1.14: X|Z collisions change
- 1.14: climbing without block (press jump)
- 1.14: sneaking height changed
- 1.14: sprint while sneaking under block?
- 1.14: forced under block (with piston). Enters sneaking or swimming pose
- < 1.14: blip https://www.mcpk.wiki/wiki/Blip
- 1.15: slipperiness changed
- 1.16: flowing lava pushed player
