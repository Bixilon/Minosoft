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

package de.bixilon.minosoft.gui.rendering.sky.planet.scatter

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.sky.SkyChildRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.sky.planet.SunRenderer
import de.bixilon.minosoft.util.KUtil.minosoft

class SunScatterRenderer(
    private val sky: SkyRenderer,
    private val sun: SunRenderer,
) : SkyChildRenderer {
    private val shader = sky.renderSystem.createShader(minosoft("sky/scatter/sun"))
    private val mesh = SunScatterMesh(sky.renderWindow)
    private var matrix = Mat4()

    private fun calculateMatrix() {
        val matrix = Mat4(sky.matrix)

        matrix.rotateAssign((sun.calculateAngle() + 90.0f).rad, Vec3(0, 0, 1))

        this.matrix = matrix
    }

    override fun init() {
        shader.load()
    }

    override fun postInit() {
        mesh.load()
    }

    override fun draw() {
        calculateMatrix()
        shader.use()
        shader.setMat4("uScatterMatrix", matrix)
        mesh.draw()
    }
}
