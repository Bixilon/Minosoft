/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.sky.box

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.i.MVec3i
import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.math.Trigonometry.sin
import de.bixilon.kutil.math.simple.FloatMath.clamp
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.rgb
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3fUtil.interpolateLinear
import de.bixilon.minosoft.util.KUtil
import kotlin.math.exp
import kotlin.time.Duration

class SkyboxColor(
    val sky: SkyRenderer,
) {
    private var lastStrike = TimeUtil.NULL
    private var strikeDuration = Duration.ZERO


    var lightning = 0.0f
        private set
    var baseColor: RGBColor? = null

    var color: RGBColor = DEFAULT_SKY_COLOR
        private set

    private fun calculateBiomeAvg(average: (Biome) -> RGBColor?): RGBColor? {
        val entity = sky.session.camera.entity
        val eyePosition = entity.renderInfo.eyePosition
        val chunk = entity.physics.positionInfo.chunk ?: return null

        var radius = sky.profile.biomeRadius
        radius *= radius

        var red = 0
        var green = 0
        var blue = 0
        var count = 0

        val offset = MVec3i(eyePosition.x.toInt() - (chunk.position.x shl 4), eyePosition.y.toInt(), eyePosition.z.toInt() - (chunk.position.z shl 4))

        val dimension = sky.session.world.dimension
        val yRange: IntRange

        if (dimension.supports3DBiomes) {
            if (offset.y - radius < dimension.minY) {
                offset.y = dimension.minY
                yRange = IntRange(0, radius)
            } else if (offset.y + radius > dimension.maxY) {
                offset.y = dimension.maxY
                yRange = IntRange(-radius, 0)
            } else {
                yRange = IntRange(-radius, radius)
            }
        } else {
            yRange = 0..1
        }

        for (xOffset in -radius..radius) {
            for (yOffset in yRange) {
                for (zOffset in -radius..radius) {
                    if (xOffset * xOffset + yOffset * yOffset + zOffset * zOffset > radius) {
                        continue
                    }
                    val blockPosition = BlockPosition(offset.x + xOffset, offset.y + yOffset, offset.z + zOffset)
                    val neighbour = chunk.neighbours.traceChunk(blockPosition.chunkPosition) ?: continue
                    val biome = neighbour.getBiome(blockPosition.inChunkPosition) ?: continue

                    count++
                    val color = average.invoke(biome) ?: continue
                    red += color.red
                    green += color.green
                    blue += color.blue
                }
            }
        }

        if (count == 0) {
            return null
        }
        return RGBColor(red / count, green / count, blue / count)
    }

    private fun updateLightning() {
        val duration = this.strikeDuration
        val delta = now() - lastStrike
        if (delta > duration) {
            this.lightning = 0.0f
            return
        }

        val progress = (delta / duration).toFloat()

        var intensity: Float

        if (progress < LIGHTNING_PEAK_TIME) {
            intensity = KUtil.smoothstep(progress / LIGHTNING_PEAK_TIME)
        } else {
            val decay = (progress - LIGHTNING_PEAK_TIME) / (1.0f - LIGHTNING_PEAK_TIME)
            intensity = exp(-8.0f * decay)
        }

        if (progress < 0.5f) {
            val flickerTime = (delta.inWholeMilliseconds / 20.0f)
            val flicker = 0.9f + 0.1f * sin(flickerTime * PIf * 2.0f * 4f)
            intensity *= flicker
        }

        this.lightning = intensity.clamp(0.0f, 1.0f)
    }

    fun lightning(original: Vec3f): Vec3f {
        updateLightning()
        return interpolateLinear(this.lightning, original, LIGHTNING_COLOR)
    }

    private fun calculate(): RGBColor {
        sky.context.camera.fog.state.color?.let { return it.rgb() }
        val properties = sky.effects

        if (properties.fixedTexture != null) {
            // sky is a texture, no color (e.g. end)
            return DEFAULT_SKY_COLOR
        }
        if (!properties.daylightCycle) {
            // no daylight cycle (e.g. nether)
            return calculateBiomeAvg { it.fogColor } ?: DEFAULT_SKY_COLOR // ToDo: Optimize
        }
        // TODO: Check if wither is present


        val base = this.baseColor ?: DEFAULT_SKY_COLOR
        return base
    }

    fun update(): RGBColor {
        this.updateLightning()
        val color = calculate()
        this.color = color
        return color
    }

    fun onStrike(duration: Duration) {
        lastStrike = now()
        strikeDuration = duration
    }

    fun updateBase() {
        baseColor = calculateBiomeAvg(Biome::skyColor)
    }

    companion object {
        const val LIGHTNING_PEAK_TIME = 0.02f
        val LIGHTNING_COLOR = Vec3f(1.0f, 1.0f, 1.0f)

        val DEFAULT_SKY_COLOR = "#ecff89".rgb()
    }
}
