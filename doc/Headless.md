# Headless

Minosoft has two gui modules, the main gui (so called eros) and the rendering.
You can turn off both individually from each other.

## Start arguments

### Eros

To disable eros, just add `--no-eros` as start argument

### Rendering

To disable rendering, just add `--no-rendering` as start argument.

## Disable all gui

To disable all gui components you can also just add `--headless` as argument.
If present, eros and rendering will be disabled.

## Auto connect

Common options:

| Argument             | Description                                                                                                                                                                      | Example  | Default              |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|----------------------|
| `--protocol-version` | Use a specific version to connect                                                                                                                                                | `1.20.4` | `automatic`          |
| `--account`          | UUID of the account to use. You can create an account with the `account` cli command. If the account does not exist, a temporary account with that name is automatically created | `moritz` | Username of computer |

### Local

| Argument            | Description                  | Example                 | Default  |
|---------------------|------------------------------|-------------------------|----------|
| `--local`           | Set connection mode to local | -                       | -        |
| `--world-generator` | Type of the world generator  | `debug`, `flat`, `void` | `debug`  |
| `--world-storage`   | Type of the world storage    | `debug`, `memory`       | `memory` |

Example: `java -jar Minosoft.jar --local`

### Server

| Argument    | Description                                | Example       | Default |
|-------------|--------------------------------------------|---------------|---------|
| `--connect` | Set connection mode to local               | -             | -       |
| `--address` | Address (with optional port) to connect to | `hypixel.net` | -       |

Example: `java -jar Minosoft.jar --connect --address=hypixel.net --protocol-version=1.19.4 --account=9e6ce7c5-40d3-483e-8e5a-b6350987d65f`

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

- `session list`
- `session disconnect @`
- `session select 1` (The current selected session is used for session specific other commands)

### Say

- `say Moritz is the best developer ever!`

There are even more commands, those are just examples.
