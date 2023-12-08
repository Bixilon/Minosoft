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

import de.bixilon.kutil.math.simple.FloatMath.square
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.source.SpatialBiomeArray
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
        val offset = getBiomeOffset(seed, x, y, z) // TODO: minecraft supplies absolut values here, hence the biome noise is broken
        val biomeX = x + unpackX(offset)
        val biomeY = y + unpackY(offset)
        val biomeZ = z + unpackZ(offset)

        val biomeChunk = chunk.neighbours.trace(biomeX shr 4, biomeZ shr 4)

        return biomeChunk?.biomeSource?.get(biomeX and 0x0F, biomeY, biomeZ and 0x0F)
    }

    fun getBiomeOffset(seed: Long, x: Int, y: Int, z: Int): Int {
        // all xyz coordinates are from 0..15

        // target biome can also be on the negative side, offset by -2
        val cX = x - 2
        val cY = y - 2
        val cZ = z - 2

        // array source
        val sX = cX shr 2
        val sY = cY shr 2
        val sZ = cZ shr 2

        // in array
        val iX = (cX and 0x03) / 4.0f
        val iY = (cY and 0x03) / 4.0f
        val iZ = (cZ and 0x03) / 4.0f

        var minXYZ = 0
        var minDistance = Float.POSITIVE_INFINITY

        for (xyz in 0 until 2 * 2 * 2) {
            var uX = sX
            var offsetX = iX
            if (xyz and 0x04 != 0) {
                uX++
                offsetX -= 1.0f
            }

            var uY = sY
            var offsetY = iY
            if (xyz and 0x02 != 0) {
                uY++
                offsetY -= 1.0f
            }

            var uZ = sZ
            var offsetZ = iZ
            if (xyz and 0x01 != 0) {
                uZ++
                offsetZ -= 1.0f
            }


            val distance = noiseDistance(seed, uX, uY, uZ, offsetX, offsetY, offsetZ)
            if (distance > minDistance) continue
            minXYZ = xyz
            minDistance = distance
        }

        var biomeX = sX
        if (minXYZ and 0x04 != 0) {
            biomeX++
        }
        var biomeY = sY
        if (minXYZ and 0x02 != 0) {
            biomeY++
        }
        var biomeZ = sZ
        if (minXYZ and 0x01 != 0) {
            biomeZ++
        }

        return pack(biomeX - x, biomeY - y, biomeZ - z)
    }


    private fun noiseDistance(seed: Long, x: Int, y: Int, z: Int, offsetX: Float, offsetY: Float, offsetZ: Float): Float {
        var ret = mix(seed, x, y, z)

        val noiseX = nextNoiseOffset(ret); ret = next(ret, seed)
        val noiseY = nextNoiseOffset(ret); ret = next(ret, seed)
        val noiseZ = nextNoiseOffset(ret)

        return (offsetX + noiseX).square() + (offsetY + noiseY).square() + (offsetZ + noiseZ).square()
    }

    private fun mix(seed: Long, x: Int, y: Int, z: Int): Long {
        var mixed = seed

        val xL = x.toLong()
        val yL = y.toLong()
        val zL = z.toLong()

        mixed = next(mixed, xL)
        mixed = next(mixed, yL)
        mixed = next(mixed, zL)
        mixed = next(mixed, xL)
        mixed = next(mixed, yL)
        mixed = next(mixed, zL)
        return mixed
    }

    private fun nextNoiseOffset(seed: Long): Float {
        val floor = Math.floorMod((seed shr 24), SpatialBiomeArray.SIZE.toLong()).toInt()

        // return ((floor - (SpatialBiomeArray.SIZE / 2)) / SpatialBiomeArray.SIZE.toDouble()) * 0.9
        return (1.0f / 1137.0f) * floor + (-0.45f) // roughly equivalent and minimal faster
    }

    // https://en.wikipedia.org/wiki/Linear_congruential_generator
    private fun next(seed: Long): Long {
        return seed * (seed * 6364136223846793005L + 1442695040888963407L)
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
