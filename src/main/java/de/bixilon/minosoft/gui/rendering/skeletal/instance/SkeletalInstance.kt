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

package de.bixilon.minosoft.gui.rendering.skeletal.instance

import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.reset
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY_INSTANCE
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.invoke
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

class SkeletalInstance(
    val context: RenderContext,
    val model: BakedSkeletalModel,
    val transform: TransformInstance,
) {
    val animation = AnimationManager(this)
    var matrix = Mat4()


    fun draw(light: Int) {
        context.system.reset(faceCulling = false)
        val shader = context.skeletal.lightmapShader
        shader.use()
        shader.light = light
        draw(shader)
    }

    fun draw(tint: RGBAColor) {
        context.system.reset(faceCulling = false)
        val shader = context.skeletal.shader
        shader.use()
        shader.tint = tint
        draw(shader)
    }

    fun draw(shader: Shader) {
        shader.use()

        context.skeletal.upload(this)
        model.mesh.draw()
    }

    fun update(time: ValueTimeMark = now()) {
        transform.reset()
        animation.draw(time)
        transform.pack(matrix)
    }

    fun update(position: Vec3, rotation: Vec3, pivot: Vec3 = Vec3.EMPTY_INSTANCE, matrix: Mat4? = null) {
        this.matrix.reset()
        this.matrix
            .translateAssign(position)
            .translateAssign(pivot)
            .rotateRadAssign(rotation)
            .translateAssign(-pivot)

        if (matrix != null) {
            this.matrix = matrix * this.matrix
        }
    }

    fun update(rotation: Vec3, matrix: Mat4? = null) {
        update(Vec3.EMPTY_INSTANCE, rotation, matrix = matrix)
    }

    fun update(position: BlockPosition, rotation: Vec3) {
        val position = Vec3(position - context.camera.offset.offset)
        position.x += 0.5f; position.z += 0.5f // models origin is the center of block origin
        update(position, rotation, BLOCK_PIVOT)
    }

    private companion object {
        val BLOCK_PIVOT = Vec3(0.0f, 0.5f, 0.0f)
    }
}
