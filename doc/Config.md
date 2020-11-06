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
    "selected": "",
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
      "mappings": "https://gitlab.com/Bixilon/minosoft/-/raw/master/data/mcdata/%s.tar.gz?inline\u003dfalse"
    }
  },
  "debug": {
    "verify-assets": true
  }
}
```
