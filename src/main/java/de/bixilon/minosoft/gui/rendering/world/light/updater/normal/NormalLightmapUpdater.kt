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

package de.bixilon.minosoft.gui.rendering.world.light.updater.normal

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.effects.DefaultStatusEffects
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.data.world.weather.WorldWeather
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.util.VecUtil.clamp
import de.bixilon.minosoft.gui.rendering.util.VecUtil.modify
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateLinear
import de.bixilon.minosoft.gui.rendering.world.light.LightmapBuffer
import de.bixilon.minosoft.gui.rendering.world.light.updater.LightmapUpdater
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

/**
 * Updates the lightmap similar to vanilla
 * This class is heavily influenced by vanilla, thus it does not aim to match vanilla and has some tweaks/improvements
 */
class NormalLightmapUpdater(
    private val connection: PlayConnection,
    private val skyRenderer: SkyRenderer?,
) : LightmapUpdater {
    private val profile = connection.profiles.rendering.light
    private var force = true
    private val nightVision = connection.registries.statusEffectRegistry[DefaultStatusEffects.NIGHT_VISION]


    init {
        connection.world::dimension.observe(this) { force = true }
    }

    override fun update(force: Boolean, buffer: LightmapBuffer) {
        val dimension = connection.world.dimension ?: return
        val skylight = dimension.hasSkyLight && dimension.effects.daylightCycle

        if (!force || !this.force) {
            if (!skylight) {
                // do not recalculate if skylight does not change (e.g. nether or end)
                return
            }
        }
        if (skylight) {
            updateBlockSky(dimension, buffer)
        } else {
            updateBlock(dimension, buffer)
        }

        this.force = false
    }

    private fun updateBlock(dimension: DimensionProperties, buffer: LightmapBuffer) {
        val gamma = profile.gamma
        val nightVision = getNightVisionStrength()

        for (block in 0 until ProtocolDefinition.LIGHT_LEVELS) {
            var color = calculateBlock(dimension.brightness[block])
            color = tweak(color, gamma, dimension.effects.brighten, nightVision)
            buffer[0, block] = color
        }
    }

    private fun updateBlockSky(dimension: DimensionProperties, buffer: LightmapBuffer) {
        val time = connection.world.time
        val weather = connection.world.weather

        val skyColors = Array(ProtocolDefinition.LIGHT_LEVELS.toInt()) { calculateSky(dimension.brightness[it], weather, time) }
        val blockColors = Array(ProtocolDefinition.LIGHT_LEVELS.toInt()) { calculateBlock(dimension.brightness[it]) }

        val gamma = profile.gamma
        val nightVision = getNightVisionStrength()

        for (sky in 0 until ProtocolDefinition.LIGHT_LEVELS) {
            for (block in 0 until ProtocolDefinition.LIGHT_LEVELS) {
                var color = combine(skyColors[sky], blockColors[block])
                color = tweak(color, gamma, dimension.effects.brighten, nightVision)
                buffer[sky, block] = color
            }
        }
    }

    private fun calculateBlock(brightness: Float): Vec3 {
        val base = Vec3(brightness, brightness * ((brightness * 0.6f + 0.4f) * 0.6f + 0.4f), brightness * (brightness * brightness * 0.6f + 0.4f))
        return base
    }

    private fun calculateDayBase(brightness: Float, progress: Float): Vec3 {
        val base = Vec3(0.98f)

        return interpolateLinear((abs(progress - 0.5f) * 2.0f), base, base * 0.9f) * brightness
    }

    private fun calculateSunset(brightness: Float, progress: Float, time: WorldTime): Vec3 {
        val day = calculateDayBase(brightness, 1.0f)
        val night = calculateNightBase(brightness, 0.0f, time)

        return interpolateLinear(progress, day, night)
    }

    private fun calculateNightBase(brightness: Float, progress: Float, time: WorldTime): Vec3 {
        val max = Vec3(0.10f, 0.10f, 0.30f)

        return interpolateLinear((abs(progress - 0.6f) + 0.4f), max * 0.1f, max) * brightness * time.moonPhase.light
    }

    private fun calculateSunrise(brightness: Float, progress: Float, time: WorldTime): Vec3 {
        val night = calculateNightBase(brightness, 1.0f, time)
        val day = calculateDayBase(brightness, 0.0f)

        return interpolateLinear(progress, night, day)
    }

    private fun calculateThunder(base: Vec3, brightness: Float, thunder: Float): Vec3 {
        val baseBrightness = base.length()

        var color = interpolateLinear(baseBrightness, Vec3(0.55f, 0.35f, 0.58f), Vec3(0.65f, 0.4f, 0.7f)) * baseBrightness * brightness * 0.3f

        skyRenderer?.let { color = interpolateLinear(brightness * 5.0f + 0.5f, color, it.box.calculateLightingStrike(color)) }
        return interpolateLinear(thunder, base, color)
    }

    private fun calculateRain(base: Vec3, brightness: Float, rain: Float): Vec3 {
        val baseBrightness = base.length()

        val color = interpolateLinear(baseBrightness, Vec3(0.4f, 0.4f, 0.8f), Vec3(0.5f, 0.5f, 0.9f)) * baseBrightness * brightness * 0.4f

        return interpolateLinear(rain, base, color)
    }

    private fun calculateSky(brightness: Float, weather: WorldWeather, time: WorldTime): Vec3 {
        var color = when (time.phase) {
            DayPhases.DAY -> calculateDayBase(brightness, time.progress)
            DayPhases.NIGHT -> calculateNightBase(brightness, time.progress, time)
            DayPhases.SUNRISE -> calculateSunrise(brightness, time.progress, time)
            DayPhases.SUNSET -> calculateSunset(brightness, time.progress, time)
        }
        if (weather.thunder > 0.0f) {
            color = calculateThunder(color, brightness, weather.thunder)
        } else if (weather.rain > 0.0f) {
            color = calculateRain(color, brightness, weather.rain)
        }

        return color.brighten(0.05f)
    }


    private fun combine(sky: Vec3, block: Vec3): Vec3 {
        val color = sky + block

        return color.clamp()
    }

    private fun tweak(
        color: Vec3,
        gamma: Float,
        brighten: Vec3?,
        nightVision: Float,
    ): Vec3 {
        var output = color
        output = applyGamma(output, gamma)

        brighten?.let { output = applyBrighten(color, brighten) }

        output = applyNightVision(output, nightVision)


        return output
    }

    private fun applyNightVision(color: Vec3, strength: Float): Vec3 {
        if (strength <= 0.0f) {
            return color
        }
        val max = maxOf(color.r, color.g, color.b)
        if (max >= 1.0f) {
            return color
        }
        return interpolateLinear(strength, color, color * (1.0f / max))
    }

    private fun applyBrighten(color: Vec3, brighten: Vec3): Vec3 {
        return interpolateLinear(0.25f, color, brighten).clamp()
    }

    private fun applyGamma(color: Vec3, gamma: Float): Vec3 {
        return interpolateLinear(gamma, color, color modify { 1.0f - (1.0f - it).pow(4) })
    }

    private fun getNightVisionStrength(): Float {
        val nightVision = connection.player.effects[this.nightVision] ?: return 0.0f
        val time = millis()
        val end = nightVision.end
        if (time > end) {
            return 0.0f
        }
        val remaining = end - time
        if (remaining > 8000) {
            return 1.0f
        }

        return 0.3f + sin(remaining / 8000.0f * PIf * 0.2f) * 0.7f
    }

    private fun Vec3.brighten(value: Float): Vec3 {
        return this * (1.0f - value) + value
    }

    private fun Vec3.clamp(): Vec3 {
        return clamp(0.0f, 1.0f)
    }
}
