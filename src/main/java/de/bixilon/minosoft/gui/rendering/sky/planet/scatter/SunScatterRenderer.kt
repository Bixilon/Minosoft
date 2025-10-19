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

package de.bixilon.minosoft.gui.rendering.sky.planet.scatter

import de.bixilon.kmath.mat.mat4.f.MMat4f
import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.math.Trigonometry.sin
import de.bixilon.kutil.primitive.FloatUtil.rad
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.sky.SkyChildRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.sky.planet.SunRenderer
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import kotlin.math.abs

class SunScatterRenderer(
    private val sky: SkyRenderer,
    private val sun: SunRenderer,
) : SkyChildRenderer {
    private val shader = sky.context.system.createShader(minosoft("sky/scatter/sun")) { SunScatterShader(it) }
    private val mesh = SunScatterMesh(sky.context)
    private var timeUpdate = true
    private var skyMatrix = Mat4f()

    private fun calculateMatrix(skyMatrix: Mat4f) = MMat4f(skyMatrix).apply {
        rotateZAssign((sun.calculateAngle() + 90.0f).rad)
    }

    override fun init() {
        shader.load()
    }

    override fun postInit() {
        mesh.load()
    }

    private fun calculateIntensity(progress: Float): Float {
        val delta = (abs(progress) * 2.0f)

        return maxOf(sin(delta * PIf / 2.0f), 0.5f)
    }

    private fun calculateSunPosition(): Vec3f {
        val matrix = MMat4f().apply {
            rotateZAssign((sun.calculateAngle() + 90.0f).rad)
        }

        val barePosition = Vec4f(1.0f, 0.128f, 0.0f, 1.0f)

        return (matrix * barePosition).xyz
    }

    override fun onTimeUpdate(time: WorldTime) {
        timeUpdate = true
    }

    override fun draw() {
        if (!sky.profile.sunScatter || sky.time.phase == DayPhases.DAY || sky.time.phase == DayPhases.NIGHT || !sky.effects.sun) {
            return
        }
        val weather = sky.session.world.weather
        val weatherLevel = maxOf(weather.rain, weather.thunder)
        if (weatherLevel >= 1.0f) {
            // maximum rain or thunder, don't render
            return
        }

        shader.use()
        if (timeUpdate || weatherLevel > 0.0f) {
            if (timeUpdate) {
                shader.sunPosition = calculateSunPosition()
                timeUpdate = false
            }
            shader.intensity = (1.0f - weatherLevel) * calculateIntensity(sky.time.progress)
        }
        val skyMatrix = sky.matrix
        if (this.skyMatrix != skyMatrix) {
            shader.scatterMatrix = calculateMatrix(skyMatrix).unsafe
            this.skyMatrix = skyMatrix
        }

        val system = sky.context.system

        system.enable(RenderingCapabilities.BLENDING)
        system.setBlendFunction(
            sourceRGB = BlendingFunctions.SOURCE_ALPHA,
            destinationRGB = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA,
            sourceAlpha = BlendingFunctions.SOURCE_ALPHA,
            destinationAlpha = BlendingFunctions.DESTINATION_ALPHA,
        )
        mesh.draw()
        system.resetBlending()
    }
}
