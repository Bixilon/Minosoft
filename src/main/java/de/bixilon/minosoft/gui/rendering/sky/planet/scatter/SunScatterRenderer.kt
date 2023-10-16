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

package de.bixilon.minosoft.gui.rendering.sky.planet.scatter

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.kotlinglm.vec4.swizzle.xyz
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.world.time.DayPhases
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.sky.SkyChildRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.sky.planet.SunRenderer
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.Z
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class SunScatterRenderer(
    private val sky: SkyRenderer,
    private val sun: SunRenderer,
) : SkyChildRenderer {
    private val shader = sky.renderSystem.createShader(minosoft("sky/scatter/sun")) { SunScatterShader(it) }
    private val mesh = SunScatterMesh(sky.context)
    private var matrix = Mat4()
    private var timeUpdate = true
    private var skyMatrix = Mat4()

    private fun calculateMatrix(skyMatrix: Mat4) {
        val matrix = Mat4(skyMatrix)

        matrix.rotateAssign((sun.calculateAngle() + 90.0f).rad, Vec3.Z)

        this.matrix = matrix
    }

    override fun init() {
        shader.load()
    }

    override fun postInit() {
        mesh.load()
    }

    private fun calculateIntensity(progress: Float): Float {
        val delta = (abs(progress) * 2.0f)

        return maxOf(sin(delta * PI.toFloat() / 2.0f), 0.5f)
    }

    private fun calculateSunPosition(): Vec3 {
        val matrix = Mat4()
        matrix.rotateAssign((sun.calculateAngle() + 90.0f).rad, Vec3.Z)

        val barePosition = Vec4(1.0f, 0.128f, 0.0f, 1.0f)

        return (matrix * barePosition).xyz
    }

    override fun onTimeUpdate(time: WorldTime) {
        timeUpdate = true
    }

    override fun draw() {
        if (!sky.profile.sunScatter || sky.time.phase == DayPhases.DAY || sky.time.phase == DayPhases.NIGHT || !sky.effects.sun) {
            return
        }
        val weather = sky.connection.world.weather
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
        if (this.skyMatrix !== skyMatrix) {
            calculateMatrix(skyMatrix)
            shader.scatterMatrix = matrix
            this.skyMatrix = skyMatrix
        }

        sky.renderSystem.enable(RenderingCapabilities.BLENDING)
        sky.renderSystem.setBlendFunction(
            sourceRGB = BlendingFunctions.SOURCE_ALPHA,
            destinationRGB = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA,
            sourceAlpha = BlendingFunctions.SOURCE_ALPHA,
            destinationAlpha = BlendingFunctions.DESTINATION_ALPHA,
        )
        mesh.draw()
        sky.renderSystem.resetBlending()
    }
}
