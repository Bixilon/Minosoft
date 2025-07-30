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

package de.bixilon.minosoft.gui.rendering.light.updater.normal

import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.math.Trigonometry.sin
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.registries.effects.vision.VisionEffect
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.data.world.weather.WorldWeather
import de.bixilon.minosoft.gui.rendering.light.LightmapBuffer
import de.bixilon.minosoft.gui.rendering.light.updater.LightmapUpdater
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.util.VecUtil.clampAssign
import de.bixilon.minosoft.gui.rendering.util.VecUtil.modify
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3fUtil.interpolateLinear
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import kotlin.math.abs
import kotlin.math.pow
import kotlin.time.TimeSource.Monotonic.ValueTimeMark
import kotlin.time.Duration.Companion.seconds

/**
 * Updates the lightmap similar to vanilla
 * This class is heavily influenced by vanilla, thus it does not aim to match vanilla and has some tweaks/improvements
 */
class NormalLightmapUpdater(
    private val session: PlaySession,
    private val skyRenderer: SkyRenderer?,
) : LightmapUpdater {
    private val profile = session.profiles.rendering.light
    private var force = true


    init {
        session.world::dimension.observe(this) { force = true }
    }

    override fun update(force: Boolean, buffer: LightmapBuffer) {
        val dimension = session.world.dimension
        val skylight = dimension.skyLight && dimension.effects.daylightCycle
        val time = now()

        if (!force || !this.force) {
            if (!skylight) {
                // do not recalculate if skylight does not change (e.g. nether or end)
                return
            }
        }
        if (skylight) {
            updateBlockSky(dimension, buffer, time)
        } else {
            updateBlock(dimension, buffer, time)
        }

        this.force = false
    }

    private fun updateBlock(dimension: DimensionProperties, buffer: LightmapBuffer, millis: ValueTimeMark) {
        val gamma = profile.gamma
        val nightVision = getNightVisionStrength(millis)

        for (block in 0 until LightLevel.LEVELS) {
            var color = calculateBlock(dimension.ambientLight[block])
            color = tweak(color, gamma, dimension.effects.brighten, nightVision)
            buffer[0, block] = color
        }
    }

    private fun updateBlockSky(dimension: DimensionProperties, buffer: LightmapBuffer, millis: ValueTimeMark) {
        val time = session.world.time
        val weather = session.world.weather

        val skyColors = Array(LightLevel.LEVELS) { calculateSky(dimension.ambientLight[it], weather, time) }
        val blockColors = Array(LightLevel.LEVELS) { calculateBlock(dimension.ambientLight[it]) }

        val gamma = profile.gamma
        val nightVision = getNightVisionStrength(millis)

        var color = Vec3f()
        for (sky in 0 until LightLevel.LEVELS) {
            for (block in 0 until LightLevel.LEVELS) {
                combine(skyColors[sky], blockColors[block], color)
                color = tweak(color, gamma, dimension.effects.brighten, nightVision)
                buffer[sky, block] = color
            }
        }
    }

    private fun calculateBlock(brightness: Float): Vec3f {
        val base = Vec3f(brightness, brightness * ((brightness * 0.6f + 0.4f) * 0.6f + 0.4f), brightness * (brightness * brightness * 0.6f + 0.4f))
        return base
    }

    private fun calculateDayBase(brightness: Float, progress: Float): Vec3f {
        val base = DAY_BASE

        return interpolateLinear((abs(progress - 0.5f) * 2.0f), base, base * 0.9f) * brightness
    }

    private fun calculateSunset(brightness: Float, progress: Float, time: WorldTime): Vec3f {
        val day = calculateDayBase(brightness, 1.0f)
        val night = calculateNightBase(brightness, 0.0f, time)

        return interpolateLinear(progress, day, night)
    }

    private fun calculateNightBase(brightness: Float, progress: Float, time: WorldTime): Vec3f {
        val max = NIGHT_MAX

        return interpolateLinear((abs(progress - 0.6f) + 0.4f), max * 0.1f, max) * brightness * time.moonPhase.light
    }

    private fun calculateSunrise(brightness: Float, progress: Float, time: WorldTime): Vec3f {
        val night = calculateNightBase(brightness, 1.0f, time)
        val day = calculateDayBase(brightness, 0.0f)

        return interpolateLinear(progress, night, day)
    }

    private fun calculateThunder(base: Vec3f, brightness: Float, thunder: Float): Vec3f {
        val baseBrightness = base.length()

        var color = interpolateLinear(baseBrightness, THUNDER_BASE, THUNDER_BRIGHT) * baseBrightness * brightness * 0.3f

        skyRenderer?.let { color = interpolateLinear(brightness * 5.0f + 0.5f, color, it.box.color.lightning(color)) }
        return interpolateLinear(thunder, base, color)
    }

    private fun calculateRain(base: Vec3f, brightness: Float, rain: Float): Vec3f {
        val baseBrightness = base.length()

        val color = interpolateLinear(baseBrightness, RAIN_BASE, RAIN_BRIGHT) * baseBrightness * brightness * 0.4f

        return interpolateLinear(rain, base, color)
    }

    private fun calculateSky(brightness: Float, weather: WorldWeather, time: WorldTime): Vec3f {
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


    private fun combine(sky: Vec3f, block: Vec3f, output: Vec3f) {
        Vec3f.plus(output, sky, block.x, block.y, block.z)

        output.clampAssign()
    }

    private fun tweak(
        color: Vec3f,
        gamma: Float,
        brighten: Vec3f?,
        nightVision: Float,
    ): Vec3f {
        var output = color
        output = applyGamma(output, gamma)

        brighten?.let { output = applyBrighten(color, brighten) }

        output = applyNightVision(output, nightVision)


        return output
    }

    private fun applyNightVision(color: Vec3f, strength: Float): Vec3f {
        if (strength <= 0.0f) {
            return color
        }
        val max = maxOf(color.x, color.y, color.z)
        if (max >= 1.0f) {
            return color
        }
        return interpolateLinear(strength, color, color * (1.0f / max))
    }

    private fun applyBrighten(color: Vec3f, brighten: Vec3f): Vec3f {
        return interpolateLinear(0.25f, color, brighten).apply { clampAssign() }
    }

    private fun applyGamma(color: Vec3f, gamma: Float): Vec3f {
        return interpolateLinear(gamma, color, color modify { 1.0f - (1.0f - it).pow(4) })
    }

    private fun getNightVisionStrength(time: ValueTimeMark): Float {
        val nightVision = session.player.effects[VisionEffect.NightVision] ?: return 0.0f
        val end = nightVision.end
        if (time > end) {
            return 0.0f
        }
        val remaining = end - time
        if (remaining > 8.seconds) {
            return 1.0f
        }

        return 0.3f + sin((remaining / 8.seconds).toFloat() * PIf * 0.2f) * 0.7f
    }

    private fun Vec3f.brighten(value: Float): Vec3f {
        return this * (1.0f - value) + value
    }

    private fun Vec3f.clampAssign() {
        this.clampAssign(0.0f, 1.0f)
    }

    private companion object {
        val DAY_BASE = Vec3f(0.98f)
        val NIGHT_MAX = Vec3f(0.10f, 0.10f, 0.30f)
        val THUNDER_BASE = Vec3f(0.55f, 0.35f, 0.58f)
        val THUNDER_BRIGHT = Vec3f(0.65f, 0.4f, 0.7f)
        val RAIN_BASE = Vec3f(0.4f, 0.4f, 0.8f)
        val RAIN_BRIGHT = Vec3f(0.5f, 0.5f, 0.9f)
    }
}
