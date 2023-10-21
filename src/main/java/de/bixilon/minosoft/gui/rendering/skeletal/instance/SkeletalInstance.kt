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

package de.bixilon.minosoft.gui.rendering.skeletal.instance

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY_INSTANCE

class SkeletalInstance(
    val context: RenderContext,
    val model: BakedSkeletalModel,
    val transform: TransformInstance,
) {
    val animation = AnimationManager(this)
    var position = Mat4()
    var light = 0xFF


    fun draw() {
        context.system.reset(faceCulling = false)
        val shader = context.skeletal.shader
        shader.use()
        shader.light = light
        draw(shader)
    }

    fun draw(shader: Shader) {
        shader.use()

        transform.reset()
        animation.draw()
        context.skeletal.upload(this)
        model.mesh.draw()
    }

    fun update(position: Vec3, rotation: Vec3, pivot: Vec3 = Vec3.EMPTY_INSTANCE) {
        this.position = Mat4()
            .translateAssign(position)
            .translateAssign(pivot)
            .rotateRadAssign(rotation)
            .translateAssign(-pivot)
    }

    fun update(position: Vec3d, rotation: Vec3) {
        update(Vec3(position - context.camera.offset.offset), rotation)
    }

    fun update(position: Vec3i, rotation: Vec3) {
        val position = Vec3(position - context.camera.offset.offset)
        position.x += 0.5f; position.z += 0.5f // models origin is the center of block origin
        update(position, rotation, BLOCK_PIVOT)
    }

    private companion object {
        val BLOCK_PIVOT = Vec3(0.0f, 0.5f, 0.0f)
    }
}
