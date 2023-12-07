/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.biome.accessor.noise

import de.bixilon.kutil.math.simple.DoubleMath.square
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk

class VoronoiBiomeAccessor(
    world: World,
    seed: Long = 0L,
) : NoiseBiomeAccessor(world, seed) {

    override fun get(x: Int, y: Int, z: Int, chunk: Chunk): Biome? {
        val biomeY = if (world.dimension.supports3DBiomes) y else 0

        return getBiome(seed, x, biomeY, z, chunk)
    }

    private fun getBiome(seed: Long, x: Int, y: Int, z: Int, chunk: Chunk): Biome? {
        val offset = getBiomeOffset(seed, x, y, z)
        val biomeX = x + unpackX(offset)
        val biomeY = y + unpackY(offset)
        val biomeZ = z + unpackZ(offset)

        val biomeChunk = chunk.neighbours.trace(biomeX shr 2, biomeZ shr 2)

        return biomeChunk?.biomeSource?.get(biomeX and 0x0F, biomeY, biomeZ and 0x0F)
    }

    private fun getBiomeOffset(seed: Long, x: Int, y: Int, z: Int): Int {
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

        return pack(biomeX - x, biomeY - y, biomeZ - z)
    }


    private fun calculateFiddle(seed: Long, x: Int, y: Int, z: Int, xFraction: Double, yFraction: Double, zFraction: Double): Double {
        var ret = seed

        ret = next(ret, x)
        ret = next(ret, y)
        ret = next(ret, z)
        ret = next(ret, x)
        ret = next(ret, y)
        ret = next(ret, z)

        val xSalt = distribute(ret)

        ret = next(ret, seed)

        val ySalt = distribute(ret)

        ret = next(ret, seed)

        val zSalt = distribute(ret)

        return (xFraction + xSalt).square() + (yFraction + ySalt).square() + (zFraction + zSalt).square()
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

    companion object {
        const val XZ_BITS = 6
        const val XZ_MASK = (1 shl XZ_BITS) - 1
        const val XZ_NEG = XZ_MASK shr 1

        const val Y_BITS = 14
        const val Y_MASK = (1 shl Y_BITS) - 1
        const val Y_NEG = Y_MASK shr 1

        inline fun pack(x: Int, y: Int, z: Int): Int {
            return ((x + XZ_NEG) and XZ_MASK) or (((z + XZ_NEG) and XZ_MASK) shl XZ_BITS) or (((y + Y_NEG) and Y_MASK) shl (XZ_BITS + XZ_BITS))
        }

        inline fun unpackX(packed: Int): Int = (packed and XZ_MASK) - XZ_NEG
        inline fun unpackY(packed: Int): Int = ((packed shr (XZ_BITS + XZ_BITS)) and Y_MASK) - Y_NEG
        inline fun unpackZ(packed: Int): Int = ((packed shr XZ_BITS) and XZ_MASK) - XZ_NEG
    }
}
