package de.bixilon.minosoft.data.world.biome.noise

import de.bixilon.minosoft.data.mappings.biomes.Biome
import de.bixilon.minosoft.data.world.ChunkPosition
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.util.MMath.square
import glm_.vec3.Vec3i

object FuzzyNoiseBiomeCalculator {

    fun getBiome(seed: Long, x: Int, y: Int, z: Int, world: World): Biome? {
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

        fun calculateFraction(a: Int, mask: Int, first: Int, second: Double): Pair<Int, Double> {
            (a and mask == 0).let {
                return Pair(
                    if (it) first else first + 1,
                    if (it) second else second - 1.0
                )
            }
        }

        fun checkMask(mask: Int, value: Int): Int {
            return if (s and mask == 0) {
                value
            } else {
                value + 1
            }
        }

        for (i in 0 until 8) {
            val (u, xFraction) = calculateFraction(i, 0x04, p, d)
            val (v, yFraction) = calculateFraction(i, 0x02, q, e)
            val (w, zFraction) = calculateFraction(i, 0x01, r, f)


            val d3 = calculateFiddle(seed, u, v, w, xFraction, yFraction, zFraction)
            if (g > d3) {
                s = i
                g = d3
            }
        }

        val biomeX = checkMask(0x04, p)
        val biomeY = checkMask(0x02, q)
        val biomeZ = checkMask(0x01, r)

        return world.getChunk(ChunkPosition(biomeX shr 2, biomeZ shr 2))?.biomeSource?.getBiome(Vec3i(biomeX, biomeY, biomeZ))
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

        return square(xFraction + xFractionSalt) + square(yFraction + yFractionSalt) + square(zFraction + zFractionSalt)
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
