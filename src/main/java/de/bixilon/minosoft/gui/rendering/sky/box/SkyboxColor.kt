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
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.MoonPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.data.world.weather.WorldWeather
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.sky.box.SkyboxRenderer.Companion.DEFAULT_SKY_COLOR
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3fUtil.interpolateLinear
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import kotlin.math.abs
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SkyboxColor(
    val sky: SkyRenderer,
) {
    private var lastStrike = TimeUtil.NULL
    private var strikeDuration = Duration.ZERO

    private var baseColor: RGBColor? = null

    var color: RGBColor? = null
        private set


    fun lightning(original: Vec3f): Vec3f {
        val duration = this.strikeDuration
        val delta = now() - lastStrike
        if (delta > duration) {
            return original
        }
        val progress = (delta / duration).toFloat()

        val sine = abs(sin(progress * PIf * (duration / 80.milliseconds).toInt()))
        return interpolateLinear(sine, original, Vec3f(1.0f))
    }

    private fun thunder(time: WorldTime, rain: Float, thunder: Float): Vec3f? {
        val rainColor = rain(time, rain) ?: return null
        val brightness = minOf(rainColor.length() * 2, 1.0f)

        val thunderColor = interpolateLinear(brightness / 8, THUNDER_BASE_COLOR, rainColor).unsafe
        thunderColor *= brightness

        return lightning(interpolateLinear(thunder, rainColor, thunderColor.unsafe))
    }

    private fun rain(time: WorldTime, rain: Float): Vec3f? {
        val clearColor = clear(time) ?: return null
        val brightness = minOf(clearColor.length(), 1.0f)

        val rainColor = interpolateLinear(brightness / 8, RAIN_BASE_COLOR, clearColor).unsafe
        rainColor *= brightness

        return interpolateLinear(rain, clearColor, rainColor.unsafe)
    }

    private fun sunrise(progress: Float, moon: MoonPhases): Vec3f? {
        val night = night(1.0f, moon) ?: return null
        val day = day(0.0f) ?: return null

        val baseColor = interpolateLinear(progress, night, day)
        var color = baseColor

        // make a bit more red/yellow
        val delta = (abs(progress - 0.5f) * 2.0f)
        val sine = maxOf(sin(delta.pow(2) * PIf / 2.0f), 0.6f)


        color = interpolateLinear(sine, SUNRISE_BASE_COLOR, color)
        color = interpolateLinear(sky.box.intensity, baseColor, color)

        return color
    }

    private fun day(progress: Float): Vec3f? {
        val base = this.baseColor?.toVec3f() ?: return null

        return interpolateLinear((abs(progress - 0.5f) * 2.0f).pow(2), base, base * 0.9f)
    }

    private fun sunset(progress: Float, moon: MoonPhases): Vec3f? {
        val night = night(0.0f, moon) ?: return null
        val day = day(1.0f) ?: return null

        val baseColor = interpolateLinear(progress, day, night)
        var color = baseColor

        // make a bit more red
        val delta = (abs(progress - 0.5f) * 2.0f)
        val sine = maxOf(sin(delta.pow(3) * PIf / 2.0f), 0.4f)

        color = interpolateLinear(sine, SUNSET_BASE_COLOR, color)
        color = interpolateLinear(sky.box.intensity, baseColor, color)

        return color
    }

    private fun night(progress: Float, moon: MoonPhases): Vec3f? {
        val base = this.baseColor?.toVec3f()?.unsafe ?: return null
        base *= 0.1

        return interpolateLinear((abs(progress - 0.5f) * 2.0f), NIGHT_BASE_COLOR, base.unsafe) * moon.light
    }

    private fun clear(time: WorldTime) = when (time.phase) {
        DayPhases.SUNRISE -> sunrise(time.progress, time.moonPhase)
        DayPhases.DAY -> day(time.progress)
        DayPhases.SUNSET -> sunset(time.progress, time.moonPhase)
        DayPhases.NIGHT -> night(time.progress, time.moonPhase)
    }

    fun calculate(weather: WorldWeather, time: WorldTime): RGBColor? {
        if (weather.thunder > 0.0f) {
            return thunder(time, weather.rain, weather.thunder)?.let { RGBColor(it) }
        }
        if (weather.raining) {
            return rain(time, weather.rain)?.let { RGBColor(it) }
        }
        return clear(time)?.let { RGBColor(it) }
    }


    fun calculate(): RGBColor? {
        sky.context.camera.fog.state.color?.let { return it.rgb() }
        val properties = sky.effects
        val time = sky.time
        if (properties.fixedTexture != null) {
            // sky is a texture, no color (e.g. end)
            return null
        }
        if (!properties.daylightCycle) {
            // no daylight cycle (e.g. nether)
            return sky.session.calculateBiomeAvg(sky.profile.biomeRadius, Biome::fogColor) ?: DEFAULT_SKY_COLOR // ToDo: Optimize
        }
        // TODO: Check if wither is present

        var weather = sky.context.session.world.weather
        if (!properties.weather) {
            weather = WorldWeather.SUNNY
        }
        return calculate(weather, time)
    }

    fun update(): RGBColor? {
        val color = calculate()
        this.color = color
        return color
    }

    fun onStrike(duration: Duration) {
        lastStrike = now()
        strikeDuration = duration
    }

    fun updateBase() {
        baseColor = sky.session.calculateBiomeAvg(sky.profile.biomeRadius, Biome::skyColor)
    }

    companion object {
        private val THUNDER_BASE_COLOR = Vec3f(0.16f, 0.18f, 0.21f)
        private val RAIN_BASE_COLOR = Vec3f(0.39f, 0.45f, 0.54f)
        private val SUNRISE_BASE_COLOR = Vec3f(0.95f, 0.78f, 0.56f)
        private val SUNSET_BASE_COLOR = Vec3f(0.95f, 0.68f, 0.56f)
        private val NIGHT_BASE_COLOR = Vec3f(0.02f, 0.04f, 0.09f)


        @Deprecated("biome sampler; biome blending branch")
        fun PlaySession.calculateBiomeAvg(_radius: Int, average: (Biome) -> RGBColor?): RGBColor? {
            val entity = camera.entity
            val eyePosition = entity.renderInfo.eyePosition
            val chunk = entity.physics.positionInfo.chunk ?: return null

            val radius = _radius * _radius

            var red = 0
            var green = 0
            var blue = 0
            var count = 0

            val offset = MVec3i(eyePosition.x.toInt() - (chunk.position.x shl 4), eyePosition.y.toInt(), eyePosition.z.toInt() - (chunk.position.z shl 4))

            val dimension = world.dimension
            val yRange: IntRange

            if (dimension.supports3DBiomes) {
                if (offset.y - _radius < dimension.minY) {
                    offset.y = dimension.minY
                    yRange = IntRange(0, _radius)
                } else if (offset.y + _radius > dimension.maxY) {
                    offset.y = dimension.maxY
                    yRange = IntRange(-_radius, 0)
                } else {
                    yRange = IntRange(-_radius, _radius)
                }
            } else {
                yRange = 0..1
            }

            for (xOffset in -_radius.._radius) {
                for (yOffset in yRange) {
                    for (zOffset in -_radius.._radius) {
                        if (xOffset * xOffset + yOffset * yOffset + zOffset * zOffset > radius) {
                            continue
                        }
                        val blockPosition = BlockPosition(offset.x + xOffset, offset.y + yOffset, offset.z + zOffset)
                        val neighbour = chunk.neighbours.traceChunk(blockPosition.chunkPosition) ?: continue
                        val biome = neighbour.getBiome(blockPosition.inChunkPosition) ?: continue

                        val color = average.invoke(biome) ?: continue
                        count++
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
    }
}
