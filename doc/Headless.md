# Headless

Minosoft has two gui modules, the main gui (so called eros) and the rendering.
You can turn off both individually from each other.

## Start arguments

### Eros

To disable eros, just add `--disable_eros` as start argument

### Rendering

To disable rendering, just add `--disable_rendering` as start argument.

## Disable all gui

To disable all gui components you can also just add `--headless` as argument.
If present, eros and rendering will be disabled.

## Auto connect

You can then (also if running with gui) use the `--auto_connect` argument to connect to servers automatically.

The argument takes up to 3 parameters, split by comma:

| Index | Comment                                                                              |
|-------|--------------------------------------------------------------------------------------|
| 0     | Server address as string (required)                                                  |
| 1     | Enforced version (optional, can be `automatic`)                                      |
| 2     | Account uuid (optional, currently selected account in eros will be used as fallback) |

Example: `java -jar Minosoft.jar --auto_connect=hypixel.net,1.19.4,9e6ce7c5-40d3-483e-8e5a-b6350987d65f`

## CLI

Minosoft features a command line shell, that can be used to manage it. Auto complete is available everywhere. It can also be accessed in the normal chat (starting with a dot `.`)

### Account

- `account add microsoft`
- `account list`
- `account list @[type=minosoft:microsoft_account]`

### Ping

- `ping hypixel.net`
- `ping hypixel.net 1.8.9`

### Connect

- `connect hypixel.net 1.19.4`

### Connection

- `connection list`
- `connection disconnect @`
- `connection select 1` (The current selected connection is used for connection specific other commands)

### Say

- `say Moritz is the best developer ever!`

There are even more commands, those are just examples.
