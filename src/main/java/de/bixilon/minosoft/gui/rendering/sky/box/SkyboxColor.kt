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

package de.bixilon.minosoft.gui.rendering.sky.box

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.math.Trigonometry.sin
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.MoonPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.data.world.weather.WorldWeather
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateLinear
import kotlin.math.abs
import kotlin.math.pow

class SkyboxColor(
    val sky: SkyRenderer,
) {
    private var lastStrike = -1L
    private var strikeDuration = -1

    private var baseColor: RGBColor? = null

    var color: RGBColor? = null
        private set

    private fun calculateBiomeAvg(average: (Biome) -> RGBColor?): RGBColor? {
        var radius = sky.profile.biomeRadius
        radius *= radius

        var red = 0
        var green = 0
        var blue = 0
        var count = 0

        val entity = sky.connection.camera.entity
        val eyePosition = entity.renderInfo.eyePosition
        val chunk = entity.physics.positionInfo.chunk ?: return null
        val offset = Vec3i(eyePosition)

        val dimension = sky.connection.world.dimension
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
                    val x = offset.x + xOffset
                    val y = offset.y + yOffset
                    val z = offset.z + zOffset
                    val neighbour = chunk.neighbours.trace(Vec2i(x shr 4, z shr 4)) ?: continue
                    val biome = neighbour.getBiome(x and 0x0F, y, z and 0x0F) ?: continue

                    count++
                    val color = average(biome) ?: continue
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


    fun calculateLightingStrike(original: Vec3): Vec3 {
        val duration = this.strikeDuration
        val delta = millis() - lastStrike
        if (delta > duration) {
            return original
        }
        val progress = delta / duration.toFloat()

        val sine = abs(sin(progress * PIf * (duration / 80).toInt()))
        return interpolateLinear(sine, original, Vec3(1.0f))
    }

    private fun calculateThunder(time: WorldTime, rain: Float, thunder: Float): Vec3? {
        val rainColor = calculateRain(time, rain) ?: return null
        val brightness = minOf(rainColor.length() * 2, 1.0f)

        val thunderColor = interpolateLinear(brightness / 8, THUNDER_BASE_COLOR, rainColor)
        thunderColor *= brightness

        return calculateLightingStrike(interpolateLinear(thunder, rainColor, thunderColor))
    }

    private fun calculateRain(time: WorldTime, rain: Float): Vec3? {
        val clearColor = calculateClear(time) ?: return null
        val brightness = minOf(clearColor.length(), 1.0f)

        val rainColor = interpolateLinear(brightness / 8, RAIN_BASE_COLOR, clearColor)
        rainColor *= brightness

        return interpolateLinear(rain, clearColor, rainColor)
    }

    private fun calculateSunrise(progress: Float, moon: MoonPhases): Vec3? {
        val night = calculateNight(1.0f, moon) ?: return null
        val day = calculateDaytime(0.0f) ?: return null

        val baseColor = interpolateLinear(progress, night, day)
        var color = Vec3(baseColor)

        // make a bit more red/yellow
        val delta = (abs(progress - 0.5f) * 2.0f)
        val sine = maxOf(sin(delta.pow(2) * PIf / 2.0f), 0.6f)


        color = interpolateLinear(sine, SUNRISE_BASE_COLOR, color)
        color = interpolateLinear(sky.box.intensity, baseColor, color)

        return color
    }

    private fun calculateDaytime(progress: Float): Vec3? {
        val base = this.baseColor?.toVec3() ?: return null

        return interpolateLinear((abs(progress - 0.5f) * 2.0f).pow(2), base, base * 0.9f)
    }

    private fun calculateSunset(progress: Float, moon: MoonPhases): Vec3? {
        val night = calculateNight(0.0f, moon) ?: return null
        val day = calculateDaytime(1.0f) ?: return null

        val baseColor = interpolateLinear(progress, day, night)
        var color = Vec3(baseColor)

        // make a bit more red
        val delta = (abs(progress - 0.5f) * 2.0f)
        val sine = maxOf(sin(delta.pow(3) * PIf / 2.0f), 0.4f)

        color = interpolateLinear(sine, SUNSET_BASE_COLOR, color)
        color = interpolateLinear(sky.box.intensity, baseColor, color)

        return color
    }

    private fun calculateNight(progress: Float, moon: MoonPhases): Vec3? {
        val base = this.baseColor?.toVec3() ?: return null
        base *= 0.1

        return interpolateLinear((abs(progress - 0.5f) * 2.0f), NIGHT_BASE_COLOR, base) * moon.light
    }

    private fun calculateClear(time: WorldTime): Vec3? {
        return when (time.phase) {
            DayPhases.SUNRISE -> calculateSunrise(time.progress, time.moonPhase)
            DayPhases.DAY -> calculateDaytime(time.progress)
            DayPhases.SUNSET -> calculateSunset(time.progress, time.moonPhase)
            DayPhases.NIGHT -> calculateNight(time.progress, time.moonPhase)
        }
    }

    fun update(weather: WorldWeather, time: WorldTime): RGBColor? {
        if (weather.thunder > 0.0f) {
            return calculateThunder(time, weather.rain, weather.thunder)?.let { RGBColor(it) }
        }
        if (weather.raining) {
            return calculateRain(time, weather.rain)?.let { RGBColor(it) }
        }
        return calculateClear(time)?.let { RGBColor(it) }
    }


    fun update(): RGBColor? {
        val properties = sky.effects
        val time = sky.time
        if (properties.fixedTexture != null) {
            // sky is a texture, no color (e.g. end)
            return null
        }
        if (!properties.daylightCycle) {
            // no daylight cycle (e.g. nether)
            return calculateBiomeAvg { it.fogColor } // ToDo: Optimize
        }
        // TODO: Check if wither is present

        var weather = sky.context.connection.world.weather
        if (!properties.weather) {
            weather = WorldWeather.SUNNY
        }
        return update(weather, time)
    }

    fun onStrike(duration: Int) {
        lastStrike = millis()
        strikeDuration = duration
    }

    fun updateBase() {
        baseColor = calculateBiomeAvg(Biome::skyColor)
    }

    companion object {
        private val THUNDER_BASE_COLOR = Vec3(0.16f, 0.18f, 0.21f)
        private val RAIN_BASE_COLOR = Vec3(0.39f, 0.45f, 0.54f)
        private val SUNRISE_BASE_COLOR = Vec3(0.95f, 0.78f, 0.56f)
        private val SUNSET_BASE_COLOR = Vec3(0.95f, 0.68f, 0.56f)
        private val NIGHT_BASE_COLOR = Vec3(0.02f, 0.04f, 0.09f)
    }
}
