# Support game/protocol versions

## General

Minosoft only supports Minecraft Java Edition at the moment from 1.7 upwards.
Versions before the minecraft netty rewrite (1.7; 13w41b) are currently not supported.

## Releases

| Name       | State | Comment                                                                                      |
|------------|-------|----------------------------------------------------------------------------------------------|
| 1.20.x     | ✅     | Works great [#111](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/111)                  |
| 1.19.x     | ✅     | signature not 100% implemented [#83](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/83) |
| 1.18.x     | ✅     | best version supported                                                                       |
| 1.17.x     | ✅     |                                                                                              |
| **1.16.x** | ✅     | lts                                                                                          |
| 1.15.x     | ✅     | eol                                                                                          |
| 1.14.x     | ✅     | eol, some minor bugs                                                                         |
| 1.13.x     | ❌     | eol, no mappings  [#40](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/40)              |
| **1.12.x** | ❌     | lts, pre flattening  [#26](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/26)           |
| 1.11.x     | ❌     | eol, pre flattening  [#26](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/26)           |
| 1.10.x     | ❌     | eol, pre flattening  [#26](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/26)           |
| 1.9.x      | ❌     | eol, pre flattening  [#26](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/26)           |
| **1.8.x**  | ❌     | lts, pre flattening  [#26](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/26)           |
| 1.7.x      | ❌     | eol, pre flattening  [#26](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/26)           |

Versions marked as "eol" are end of life. That means that they are not high priority in minosoft, but their support will never get removed.  
Versions marked with "lts" are long term supported. They (and the latest one) will always be high priority supported.

## April fool

Mojang mostly did not change the version number of them, thus making it impossible to distinct between snapshots and them.  
Also it is quite heavy to implement them. They might work, but are not supported at all.

## Snapshots

Snapshots are technically supported, but only the latest one should be used as reference, all previous ones might fail or not even work.
It is **not** recommended using them (and honestly: who cares about those versions?).
