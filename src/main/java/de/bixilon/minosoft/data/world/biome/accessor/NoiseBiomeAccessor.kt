/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.biome.accessor

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.math.simple.DoubleMath.square
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.biome.source.SpatialBiomeArray
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class NoiseBiomeAccessor(
    private val connection: PlayConnection,
) {
    private val world = connection.world
    private var fastNoise = false

    init {
        val profile = connection.profiles.rendering
        profile.performance::fastBiomeNoise.profileWatch(this, true, profile = profile) { fastNoise = it }
    }

    fun getBiome(x: Int, y: Int, z: Int, chunkPositionX: Int, chunkPositionZ: Int, chunk: Chunk, neighbours: Array<Chunk>?): Biome? {
        val biomeY = if (world.dimension?.supports3DBiomes == true) {
            y
        } else {
            0
        }

        if (fastNoise) {
            chunk.biomeSource?.let {
                if (it !is SpatialBiomeArray) {
                    return null
                }

                return it.data[(biomeY and 0x0F) shr 2 and 0x3F shl 4 or ((z and 0x0F) shr 2 and 0x03 shl 2) or ((x and 0x0F) shr 2 and 0x03)]
            }
            return null
        }

        return getBiome(world.hashedSeed, x, biomeY, z, chunkPositionX, chunkPositionZ, chunk, neighbours)
    }


    private fun getBiome(seed: Long, x: Int, y: Int, z: Int, chunkPositionX: Int, chunkPositionZ: Int, chunk: Chunk, neighbours: Array<Chunk>?): Biome? {
        val m = x - 2
        val n = y - 2
        val o = z - 2

        val p = m shr 2
        val q = n shr 2
        val r = o shr 2

        val d = (m and 0x03) / 4.0
        val e = (n and 0x03) / 4.0
        val f = (o and 0x03) / 4.0

        var s = 0
        var g = Double.POSITIVE_INFINITY

        for (i in 0 until 8) {
            var u = p
            var xFraction = d
            if (i and 0x04 != 0) {
                u++
                xFraction -= 1.0
            }

            var v = q
            var yFraction = e
            if (i and 0x02 != 0) {
                v++
                yFraction -= 1.0
            }

            var w = r
            var zFraction = f
            if (i and 0x01 != 0) {
                w++
                zFraction -= 1.0
            }


            val d3 = calculateFiddle(seed, u, v, w, xFraction, yFraction, zFraction)
            if (g > d3) {
                s = i
                g = d3
            }
        }

        var biomeX = p
        if (s and 0x04 != 0) {
            biomeX++
        }
        var biomeY = q
        if (s and 0x02 != 0) {
            biomeY++
        }
        var biomeZ = r
        if (s and 0x01 != 0) {
            biomeZ++
        }

        var biomeChunk: Chunk? = null
        val biomeChunkX = biomeX shr 2
        val biomeChunkZ = biomeZ shr 2

        if (neighbours == null) {
            return world[Vec2i(biomeChunkX, biomeChunkZ)]?.biomeSource?.getBiome(biomeX, biomeY, biomeZ)
        }

        val deltaChunkX = biomeChunkX - chunkPositionX
        val deltaChunkZ = biomeChunkZ - chunkPositionZ

        when (deltaChunkX) {
            0 -> when (deltaChunkZ) {
                0 -> biomeChunk = chunk
                -1 -> biomeChunk = neighbours[3]
                1 -> biomeChunk = neighbours[4]
            }
            -1 -> when (deltaChunkZ) {
                0 -> biomeChunk = neighbours[1]
                -1 -> biomeChunk = neighbours[0]
                1 -> biomeChunk = neighbours[2]
            }
            1 -> when (deltaChunkZ) {
                0 -> biomeChunk = neighbours[6]
                -1 -> biomeChunk = neighbours[5]
                1 -> biomeChunk = neighbours[7]
            }
        }

        return biomeChunk?.biomeSource?.getBiome(biomeX, biomeY, biomeZ)
    }

    private fun calculateFiddle(seed: Long, x: Int, y: Int, z: Int, xFraction: Double, yFraction: Double, zFraction: Double): Double {
        var ret = seed

        ret = next(ret, x)
        ret = next(ret, y)
        ret = next(ret, z)
        ret = next(ret, x)
        ret = next(ret, y)
        ret = next(ret, z)

        val xFractionSalt = distribute(ret)

        ret = next(ret, seed)

        val yFractionSalt = distribute(ret)

        ret = next(ret, seed)

        val zFractionSalt = distribute(ret)

        return (xFraction + xFractionSalt).square() + (yFraction + yFractionSalt).square() + (zFraction + zFractionSalt).square()
    }

    private fun distribute(seed: Long): Double {
        val d = Math.floorMod(seed shr 24, 1024L).toInt() / 1024.0
        return (d - 0.5) * 0.9
    }

    // https://en.wikipedia.org/wiki/Linear_congruential_generator
    private fun next(seed: Long): Long {
        return seed * (seed * 6364136223846793005L + 1442695040888963407L)
    }

    private fun next(seed: Long, salt: Int): Long {
        return next(seed) + salt
    }

    private fun next(seed: Long, salt: Long): Long {
        return next(seed) + salt
    }
}
