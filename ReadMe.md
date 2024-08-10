# Minosoft Education

You are currently looking at the **education** edition of minosoft. Please choose `master` in the branch selection to get the normal edition.
To get a general overview over this project, check out `master` too.

## Education

This edition is a fork of the "normal" edition and is really stripped down. It is meant to be used by teachers or individuals
to learn object oriented programming with [Kotlin](https://kotlinlang.org/) or [Java](https://www.java.com/en/).

## Licence

This project is licenced under the GPLv3 licence, that pretty much means:

- you can use it for **free** for whatever you want
- You can not remove this copyright.

Additionally (not covered by the GPL licence) **only** for the education edition and if you just minosoft for **educational purposes**:
You **don't** need to publish your changes made to the code.

"Educational" purposes means (and only means) that you use this project to learn programming or are a teacher and want to use it in the classroom.

## Differences to the normal edition

Minosoft education is really limited. See all restrictions below (you don't want students to be distracted by video games; the focus is to learn coding).

- No multiplayer; offline playing only
- Limited world size (you can not leave the area)
- Less world interactions (attacking entities, breaking/placing blocks, ...)
- No commands, no chat
- No items, enchantments, potions, ...
- Only limited blocks (`stone`, `bedrock`, `cobblestone`, `oak_planks`, `oak_leaves`, `redstone_lamp`, ...)
- Some limited entities

Keep in mind, that this is just soft locked down, the original edition includes all those features.

Internally it is running minecraft 1.16.5 in the background.

## System requirements

- OS: Windows/Linux/macOS (FreeBSD, [Android](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/71), Chrome OS, iOS are not supported)
- CPU: x64/x86 processor (arm 64 is supported on macOS and linux)
- Memory: At least 150 MB free, 300 MB+ recommended
- GPU: Pretty much anything will work, it just needs to support OpenGL 3.3+ (gpus form 2010 up)
- Disk space: The executable (60MB) + 3 MB for assets like textures
- Java 11+
- No network is required (only for starting once, but it can also run completely offline, see #Offline)

Minosoft is really optimized and runs on really little hardware, ideal for schools/people with old computers.

Sadly due to not having opengl, the raspberry pi (or similar arm boards) are not supported, see [#77](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/77) for more details.

## Offline

Minosoft itself needs network access to download some resources (download ~20MB; on disk ~3MB) which includes language files, block models and textures.
This is because I don't own the copyright on the textures, they are directly taken from mojang.
However, you can append those resources to the jar file, for complete offline usage.
Keep in mind, that those files contain copyright protected files, you should not distribute those files in the internet (but mojang is pretty chill about it, see their [eula](https://www.minecraft.net/en-us/eula): "let other people get access to anything we've made in a way that is unfair or unreasonable.")

```
Instructions following...
```

## Customizing

You can block/allow certain features, change the world size and more using a config file. This file needs to be added to the jar file and is called `education.json`.

```
Instructions following...
```

## GUI

There is a small debug gui, where you can reset the world, run custom functions or reload the code.

## Developing (or "using")

```
Instructions following...
```


## Building

1. Install Java 11+ (e.g. `sudo apt install openjdk-11-jdk`). Windows users [download](https://www.azul.com/downloads/?package=jdk#zulu) and install java.
2. Clone this repository (`git clone --depth=1 https://gitlab.bixilon.de/bixilon/minosoft.git`) or click on download master and extract the archive.
3. Change directory (`cd minosoft`)
4. Checkout the `education` branch (`git checkout education`)
5. Build and run Minosoft with `./gradlew run` (or on windows `./gradlew.bat run`). Alternatively just click on `run.cmd`. If any errors occur, feel free to contact me or open an issue
6. Optional: Build a jar with `./gradlew -Pplatform=linux -Parchitecture=aarch64 fatJar` (change architecture and os where you want to execute the file later)
   1. Create an offline and customized jar, see #Offline and #Customizing for instructions
