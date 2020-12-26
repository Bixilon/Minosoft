# Config file
There is a config file located in:
 * Windows: `%AppData%\Minosoft`
 * MacOS: `"~/Library/Application Support/Minosoft"`
 * Linux (and all others): `~\Minosoft`

## Example
```json
{
  "general": {
    "version": 1,
    "log-level": "VERBOSE",
    "language": "en_US"
  },
  "game": {
    "render-distance": 12
  },
  "network": {
    "fake-network-brand": false,
    "show-lan-servers": true
  },
  "accounts": {
    "selected": "SECRET",
    "client-token": "SECRET",
    "entries": {
      "SECRET": {
        "accessToken": "SECRET",
        "userId": "SECRET",
        "uuid": "9e6ce7c5-40d3-483e-8e5a-b6350987d65f",
        "playerName": "Bixilon",
        "userName": "SECRET"
      }
    }
  },
  "servers": {
    "entries": {
      "1": {
        "id": 1,
        "name": "A Minosoft server",
        "address": "localhost",
        "favicon": "<Base 64 encoded png>",
        "version": -1
      }
    }
  },
  "download": {
    "urls": {
      "resources": "https://gitlab.com/Bixilon/minosoft/-/raw/development/data/resources/%s/%s.tar.gz?inline=false"
    }
  },
  "debug": {
    "verify-assets": true
  }
}
```

## General
 - `version` The current version of the config. Used for migration between versions. A new version will be tagged, once a new release of minosoft is there, and the format of the config changed.
 - `log-level` Self explaining, valid log levels are defined in [LogLevels.java](/src/main/java/de/bixilon/minosoft/logging/LogLevels.java).
 - `language` Self explaining. All values are valid, if the specific language cannot be loaded (or specific strings are not available), `en_US` will be used (as fallback). 

## Network
 - `fake-network-brand` Minosoft send its brand to the server. If true, minosoft will say, that we use standard `vanilla`.
 - `show-lan-servers` If true, minosoft will listen for lan servers (singleplayer and share to LAN) and show them in the server list.

## Accounts
 - `selected` userId of the current selected account, can be empty
 - `client-token` A random uuid (generated at first startup), used as unique identifier for all authentication communication with mojang.
 - `entries` An account array:
 
### Account
```json
{
  "<userId>": {
    "accessToken": "<access Token>",
    "userId": "<userId>",
    "uuid": "<UUID of player with dashes>",
    "playerName": "<Player name>",
    "userName": "<Mojang email address>"
  }
}
```

## Servers
 - `entries` A server array:
 
### Server
```json
{
  "<Server id>": {
    "id": <Server ID>,
    "name": "<Server name>",
    "address": "<Server address>",
    "favicon": "<Base 64 encoded png>",
    "version": -1
  }
}
```
## Download
 - `url`
   - `mappings` URL for resources. For example mappings. The data is in a folder starting with the first digs of the hash, followed by the hash
                 The URL must contain .tar.gz files named after minecraft versions (e.g. `0a/0aeb75059ef955d4cf2b9823d15775d0eacb13d5.tar.gz`). 

## Debug
 - `verify-assets` If true, minosoft will check the sha1 of every asset. Must be false, if you want to modify assets. (Should be true, can be false, if you want to improve the start time)
