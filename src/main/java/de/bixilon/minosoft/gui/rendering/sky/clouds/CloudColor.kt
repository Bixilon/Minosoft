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

package de.bixilon.minosoft.gui.rendering.sky.clouds

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.math.Trigonometry.sin
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.MoonPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.data.world.weather.WorldWeather
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateLinear
import kotlin.math.abs
import kotlin.math.pow

class CloudColor(
    val sky: SkyRenderer,
) {

    private fun normal(time: WorldTime): Vec3 {
        return when (time.phase) {
            DayPhases.DAY -> day(time.progress)
            DayPhases.NIGHT -> night(time.progress, time.moonPhase)
            DayPhases.SUNRISE -> sunrise(time.progress, time.moonPhase)
            DayPhases.SUNSET -> sunset(time.progress, time.moonPhase)
        }
    }

    private fun rain(time: WorldTime, rain: Float, thunder: Float): Vec3 {
        val normal = normal(time)
        val brightness = normal.length()
        var color = RAIN_COLOR
        color = color * maxOf(1.0f - thunder, 0.4f)

        return interpolateLinear(maxOf(rain, thunder), normal, color) * brightness * 0.8f
    }


    private fun day(progress: Float): Vec3 {
        return interpolateLinear((abs(progress - 0.5f) * 2).pow(2), DAY_COLOR, DAY_COLOR * 0.8f)
    }

    private fun night(progress: Float, moon: MoonPhases): Vec3 {
        return interpolateLinear((abs(progress - 0.5f) * 2).pow(2), NIGHT_COLOR, DAY_COLOR * 0.2f) * moon.light
    }

    private fun sunrise(progress: Float, moon: MoonPhases): Vec3 {
        val night = night(1.0f, moon)
        val day = day(0.0f)

        val base = interpolateLinear(progress, night, day)
        var color = Vec3(base)
        val sine = maxOf(sin((abs(progress - 0.5f) * 2.0f).pow(2) * PIf / 2.0f), 0.4f)

        color = interpolateLinear(sine, SUNRISE_COLOR, color)
        color = interpolateLinear(sky.box.intensity, base, color)

        return color
    }

    private fun sunset(progress: Float, moon: MoonPhases): Vec3 {
        val day = day(1.0f)
        val night = night(0.0f, moon)

        val base = interpolateLinear(progress, day, night)
        var color = Vec3(base)


        val sine = maxOf(sin((abs(progress - 0.5f) * 2.0f).pow(3) * PIf / 2.0f), 0.1f)

        color = interpolateLinear(sine, SUNSET_COLOR, color)
        color = interpolateLinear(sky.box.intensity, base, color)

        return color
    }


    private fun calculate(weather: WorldWeather, time: WorldTime): Vec3 {
        if (sky.effects.weather && (weather.rain > 0.0f || weather.thunder > 0.0f)) {
            return rain(time, weather.rain, weather.thunder)
        }
        return normal(time)
    }


    fun calculate(): Vec3 {
        return calculate(sky.connection.world.weather, sky.time)
    }

    companion object {
        private val RAIN_COLOR = Vec3(0.31f, 0.35f, 0.40f)
        private val SUNRISE_COLOR = Vec3(0.85f, 0.68f, 0.36f)
        private val DAY_COLOR = Vec3(0.95f, 0.97f, 0.97f)
        private val SUNSET_COLOR = Vec3(1.0f, 0.75f, 0.55f)
        private val NIGHT_COLOR = Vec3(0.08f, 0.13f, 0.18f)
    }
}
