/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.tint.sampler.gaussian

import kotlin.math.exp

// who would have though that the computer vision course at university would help me someday...
object GaussianKernel {
    const val MAX_RADIUS = 15
    val SINGLE = listOf(GaussianSample(0, 0, 0, 1)).pack()
    private val cache2D: Array<GaussianSampleList?> = arrayOfNulls(MAX_RADIUS)
    private val cache3D: Array<GaussianSampleList?> = arrayOfNulls(MAX_RADIUS)

    private fun List<GaussianSample>.pack(): GaussianSampleList {
        return GaussianSampleList(this.map { it.raw }.toIntArray())
    }

    private fun getMinimumWeight(radius: Int) = when {
        radius <= 0 -> throw IllegalArgumentException("Radius must be > 1")
        radius <= 5 -> 3
        radius <= 8 -> 2
        else -> 1
    }

    private fun build(radius: Int, vertical: Boolean): GaussianSampleList {
        val minWeight = getMinimumWeight(radius) / 255.0f
        val samples: ArrayList<GaussianSample> = ArrayList()

        val radius2 = radius * radius

        val sigma = radius / 2.0f
        val sigma2 = 2.0f * sigma * sigma

        val yRange = if (vertical) radius else 0

        for (x in -radius..radius) {
            val distanceX = x * x
            for (y in -yRange..yRange) {
                val distanceXY = distanceX + y * y
                for (z in -radius..radius) {
                    val distanceXYZ = distanceXY + z * z
                    if (distanceXYZ > radius2) continue

                    val weight = exp(-distanceXYZ / sigma2)
                    if (weight < minWeight) continue

                    samples += GaussianSample(x, y, z, (weight * 255.0f).toInt())
                }
            }
        }

        return samples.pack()
    }


    fun get2D(radius: Int): GaussianSampleList {
        val index = radius - 1
        this.cache2D[index]?.let { return it }

        val samples = build(radius, false)
        this.cache2D[index] = samples

        return samples
    }

    fun get3D(radius: Int): GaussianSampleList {
        val index = radius - 1
        this.cache3D[index]?.let { return it }

        val samples = build(radius, true)
        this.cache3D[index] = samples

        return samples
    }
}
