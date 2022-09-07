# Why Minosoft is faster than Minecraft

Yes, it is true. There are a lot of reasons, I want to explain some of them, or at least bring the idea nearer

## Kotlin over Java

Sounds like magic and it is not magic. If you use kotlin, you use a different style of programming
and that indeed can make it faster (e.g. `map[key]?.let` vs `if map.containsKey(key)) map.get(key) …`
Also language features help out here (e.g. removing recursion layers with `inline`)

## Dirty hacks

Sometimes I do dirty hacks (like a default option to disable some unneeded feature such as biome noise).
Also some stuff just gets implemented half way and some bugs (e.g. transparency) are a side effect, but they are not major.

## Consuming memory

Sounds bad, but your system memory has 1 job: getting used. With that principle in mind I can
cache a lot of things (like 3d biomes) or block states. That often makes things WAY faster

## Code simplicity

Code should be simple. I try to write everything as simple as possible, not like Minecraft.
Minecraft has a lot of pieces that not understandable. Normally simple codes is faster.

## Beging lighter

Meant in the meaning that a lot of stuff is not yet implemented. Should be a bad thing?

## Doing things different

Sometimes I am implementing things completely different from minecraft, but the result is the same
(e.g. beds, signs not as block entity)

## Modern opengl

Minosoft does everything with shaders. Minecraft often still uses old opengl (i.e. pushing matrices, ...)

## Multithreading

Minosoft is pretty much only async. Minecraft does most stuff on one thread.

# Why it could be slower

## Multiple versions

Checking stuff for every version is expensive. Only one version is easy :)

## PixLyzer

All data is dynamic and "liable". That needs to be corrected (e.g. maps over arrays for registry id mapping)
