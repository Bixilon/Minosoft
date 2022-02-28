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
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3i

class SkeletalInstance(
    val renderWindow: RenderWindow,
    blockPosition: Vec3i,
    val model: BakedSkeletalModel,
) {
    private val blockPosition = blockPosition.toVec3

    fun playAnimation(name: String) {

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

        val transforms = arrayOf(base)

        shader["uSkeletalTransforms"] = transforms
    }
}
