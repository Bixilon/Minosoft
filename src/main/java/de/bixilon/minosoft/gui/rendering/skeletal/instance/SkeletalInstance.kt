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

package de.bixilon.minosoft.gui.rendering.skeletal.instance

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import glm_.func.rad
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class SkeletalInstance(
    val renderWindow: RenderWindow,
    blockPosition: Vec3i,
    val model: BakedSkeletalModel,
) {
    private val blockPosition = blockPosition.toVec3
    private var openDelta = -90.0f
    private var closing: Boolean? = null

    fun playAnimation(name: String) {
        if (name.contains("closing")) {
            openDelta = -90.0f
            closing = true
        } else {
            openDelta = 0.0f
            closing = false
        }
    }

    fun draw() {
        renderWindow.renderSystem.reset()
        val shader = renderWindow.shaderManager.skeletalShader
        shader.use()
        setTransforms(shader)

        model.mesh.draw()
    }

    private fun setTransforms(shader: Shader) {
        val base = Mat4().translateAssign(blockPosition.toVec3)

        val origin = Vec3(0 + 8, 10, 7 + 8) / Vec3(16, 16, 16)
        val rotationX = (openDelta).rad
        if (closing != null) {
            if (closing!!) {
                openDelta += 2f
            } else {
                openDelta -= 2f
            }
            if (openDelta <= -90.0f || openDelta >= 0.0f) {
                closing = null
            }
        }

        val lid = Mat4()
        lid.translateAssign(origin)
        lid.rotateAssign(-rotationX, Vec3(1, 0, 0))
        lid.translateAssign(-origin)
        lid[3, 0] += blockPosition.x
        lid[3, 1] += blockPosition.y
        lid[3, 2] += blockPosition.z

        val transforms = arrayOf(base, lid)

        shader["uSkeletalTransforms"] = transforms
    }
}
