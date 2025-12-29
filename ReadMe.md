# Minosoft

[<img src="https://shields-io.bixilon.de/matrix/minosoft:matrix.org?style=for-the-badge">](https://matrix.to/#/#minosoft:matrix.org)
<img src="https://shields-io.bixilon.de/gitlab/pipeline-status/bixilon/minosoft?branch=master&gitlab_url=https%3A%2F%2Fgitlab.bixilon.de&style=for-the-badge">
<img src="https://shields-io.bixilon.de/badge/license-GPLv3-brightgreen?style=for-the-badge">

Minosoft is an open source minecraft client, written from scratch in kotlin (and java). It aims to bring more functionality and stability.  
(This is not a classical *clone* of minecraft, it completely re implements it!)  
(This software is not affiliated with Mojang AB, the original developer of Minecraft)

New: There is an minimal education edition available, just check out the `education` branch.

<h2>Notice: I am *not* responsible for anti cheat banned accounts, this project is still in development!</h2>

I am semi actively developing this project at the moment. That means I am working on it if I am bored or got some spare time (or sitting in the subway). I am not really adding a lot of features (that time is over),
but improving performance, refactoring and improving code (and sometimes new features land). Sometimes trying new things. I am not adding support for new protocol version, I am bored of if.
Vanilla updates get larger and larger and I honestly can't keep up.
Don't expect too much from me. I am still happy to receive feedback and contributions are always welcome. Feel free to join the matrix chat to say hello.

## Feature overview

- Rendering
- Connect with any version to any server  (1.7 - 1.20.4)
- [Bleeding edge performance (e.g. incredible start time)](/doc/Performance.md)
- Free (as far as we consider original minecraft as free) and open source
- Easy use of multiple accounts
- Multiple connections to servers in one process
- Multithreading and asynchronous loading
- [Original physics](/doc/Physics.md)
- Debugging on protocol layer
- LAN servers
- Multiple profiles (i.e. settings for servers or minosoft in general)
- Modding
- Independent, I will probably accept almost all patches
- [Headless mode](/doc/Headless.md)
- Way more stuff

(some ~~technical~~ explanation about the render system is [here](/doc/rendering/ReadMe.md)). You can find information about the architecture design [here](/doc/Architecture.md)

## System requirements

- OS: Windows/Linux/macOS (x86/x64 or arm64/aarch64)
- CPU: Multiple (4+) cores, high clock speed (2+ GHz)
- RAM: Minimum 500 MiB, 1 GiB recommended
- Disk space: 80 MiB + assets (~ 300 MiB per version)
- GPU: OpenGL 3.3+. Every modern GPU works and is recommended.
- Java 11+ (Java 8 is **not** supported).
- ~~A minecraft server (local or online)~~ (there is a flat world single player for testing purposes)

## Rendering

### Features

- Blocks
- Entities
- Block entities (e.g. signs, chests)
- HUD and GUI (inventory, menus, ...)
- Particles
- Block and skylight (custom light engine)
- World interactions (e.g. place, break, mining)
- A lot more, only listing major things here, see the screenshots

You can actually use this as a complete vanilla replacement (and speedrun minecraft), but a lot is still missing.

![Rendering](doc/img/rendering5.png)  
A world, with a ton of hud features exposed

![Rendering](doc/img/hypixel_skyblock.png)  
The Hypixel skyblock hub (don't try to make such a screenshot)

![Rendering](doc/img/afk_pool.png)  
AFK Pooling, Hit boxes, particles, ...

![Hypixel Lobby](doc/img/hypixel_lobby.png)  
Lobby of hypixel.net with entities.

![Rendering](doc/img/sunset.png)  
A beautiful sunset

![Eros](doc/img/eros.png)  
[Eros](https://en.wikipedia.org/wiki/Eros) is the main gui. You can select your account/favorite server and then connect to it. Once everything is prepared, the rendering will start.

Some screenshots are **years** old, try the latest version to see how it actually looks.

## Version support

Almost all versions (and snapshots!) between 1.7 and the latest one (1.20.4 as of writing this) are supported.
My initial goal was to support up to 1.20, that is done. 1.21.X is not supported at the moment, see the [issue](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/127).
See [Version support](/doc/VersionSupport.md) for more details.

## Modding

Works, still missing some features to make modding super easy (see e.g. [#12](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/12))

### Botting

Will be improved in the future.

## Contribution or helping out

Please do it if you are interested in it. Grab pretty much whatever you want and start developing.
(But before please take a look at [Contributing.md](/Contributing.md))

## Credits and thanking words

See [Credits](Credits.md).

## Releases and beta

No clue. Don't wait for it :)

I invested many thousands of hours in this project to make it "work". A lot of vanillas features are actually implemented, but soo much is missing.
A release normally means, that it is stable, has few (known) bugs and won't change that much in the near future.
All those "requirements" are currently not fulfilled at all. It is just way to alpha atm. The current goal always was to play bedwars with this client and it actually worked on hypixel ([#42](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/42)).

Feel free to join the [matrix channel](https://matrix.to/#/#minosoft:matrix.org) to get notified when new features arrive.

## Downloads / Installation

### Linux, Windows, macOS

The latest x64 and arm64 build gets uploaded to github actions. See [github actions](https://github.com/Bixilon/Minosoft/actions/workflows/build.yml?query=branch%3Amaster+event%3Apush+is%3Asuccess&ref=gitlab.bixilon.de)


#### Arch

Thanks to @jugendhacker you can get minosoft directly from the arch user repository (AUR): https://aur.archlinux.org/packages/minosoft-git/

## Building

1. Install Java 11+ (e.g. `sudo apt install openjdk-11-jdk`). Windows users [download](https://www.azul.com/downloads/?package=jdk#zulu) and install java.
2. Clone this repository (`git clone --depth=1 https://gitlab.bixilon.de/bixilon/minosoft.git`) or click on download master and extract the archive.
3. Change directory (`cd minosoft`)
4. Optional: Checkout a current feature branch (Warning: might be unstable; might not even build) (`git checkout <branch>`)
5. Build and run Minosoft with `./gradlew run` (or on windows `./gradlew.bat run`). Alternatively just click on `run.cmd`. If any errors occur, feel free to contact me or open an issue

## Code mirrors

- [gitlab.bixilon.de](https://gitlab.bixilon.de/bixilon/minosoft/) (Main repository)
- [GitLab](https://gitlab.com/Bixilon/minosoft)
- [GitHub](https://github.com/Bixilon/Minosoft/?ref=gitlab.bixilon.de)

This project/readme is work in progress, things may change over time.
