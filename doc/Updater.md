# Updater

Minosoft can (and will by default; it asks first) check for updates at startup.

## Update check

Minosoft fetches a configured url with the following parameters:

- current commit (if available)
- current version
- configured update channel (`stable` by default)
- os: current operating system (binaries are platform specific)
- arch: current cpu architecture (e.g. `x64` or `x86`)

### Response (v1)

The server internally checks if the user can receive updates for the version or platform. If no update is available, it responds with `204 No content`.
Otherwise it responds with `200 OK` and returns a json object (with the signature, check below):

```json
{
  "id": "Version id",
  "name": "Display text of the version",
  "stable": "Build from a tag or not",
  "page": "<Optional link to a release page>",
  "date": "Unix timestamp (in seconds) of the release date",
  "download": {
    // optional
    "url": "https:// where to download it",
    "size": <Update size in Bytes>,
    "sha512": "SHA512 hash of the binary",
    "signature": "SHA512withRSA signature of the binary"
  },
  "release_notes": "<Optional text for release notes to show in the client>"
}
```

All urls in the response **must** start with `https://` or be a localhost link.


## Update process

When an update is available, it prompts the user the install it. It tries to store the file in the current directory (if possible) or asks the user where to store it.
Once it is downloaded, minosoft asks the user to restart. It will then quit and starts the new jar.

The client will refuse to update, if the release date of the next version is lower than the version currently running (i.e. no downgrades)

## Future

- Maybe split the fat jar and download all dependencies individual (reduces size of the binary; lowers traffic)
