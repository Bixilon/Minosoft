# Why Minosoft is faster than Minecraft

Yes, it is true. There are a lot of reasons, I want to explain some of them, or at least bring the idea nearer

## Kotlin over Java

Sounds like magic and it is not magic. If you use kotlin, you use a different style of programming
and that indeed can make it faster (e.g. `map[key]?.let` vs `if map.containsKey(key)) map.get(key) …`
Also language features help out here (e.g. removing function call layers with `inline`).
Delegates can cache values (especially maps) and make observers light (at least from code perspective)

## Dirty hacks

Sometimes I do dirty hacks (like a default option to disable some unneeded feature such as biome noise).
Also some stuff just gets implemented half way and some bugs (e.g. transparency) are a side effect, but they are not major.


## Code simplicity

Code should be simple. I try to write everything as simple as possible, not like Minecraft.
Minecraft has a lot of pieces that are not understandable. Normally simple codes is faster.

## Profiling

From time to time I am profiling minosoft and analyzing the current code sequence. It sometimes is pretty
obvious why things are slow, sometimes it takes longer, but in either way I am doing changes and then
I profile them again until it is fast enough.

## Mutable objects & memory

You can profile memory in two ways:

### Usage of memory

Sounds for most daus bad, but your system memory has 1 job: getting used. With that principle in mind I can
cache a lot of things (like 3d biomes) or block states. That often makes things WAY faster.
I am using as much memory as needed to improve performance.

### Allocation rate

The higher the allocation rate, the more (useless) memory bandwidth is used and the garbage collector needs to work.
Despite loving final (i.e. immutable) things, I am reusing objects in all performance critical parts (especially in physics and rendering).
That makes things a lot faster.

## Being lighter

Meant in the meaning that a lot of stuff is not yet implemented. Should be a bad thing?

## Doing things different

Sometimes I am implementing things completely different from minecraft, but the result is the same
(e.g. beds, signs not as block entity)

## Modern opengl

Minosoft does everything with shaders and vbos (vertex buffer object).
Minecraft often still uses legacy opengl (i.e. pushing matrices, ...).

## Multithreading

Most modern cpus have 8 cores. Minecraft does most important stuff on one thread (one cpu core).
Minosoft is mostly not aware of "static threads". That means that all operations are executed
in parallel and timed that almost all operations can make use of modern cpus.

# Why it could be slower

## Multiple versions

Checking stuff for every version is expensive. Only one version is easy :)

## PixLyzer

All data is dynamic and "liable". That needs to be corrected (e.g. maps over arrays for registry id mapping)
