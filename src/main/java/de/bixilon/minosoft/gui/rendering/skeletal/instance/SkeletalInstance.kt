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

import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.kmath.mat.mat4.f.MMat4f
import de.bixilon.kmath.mat.mat4.f.Mat4Operations
import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

class SkeletalInstance(
    val context: RenderContext,
    val model: BakedSkeletalModel,
    val transform: TransformInstance,
) {
    val animation = AnimationManager(this)
    var matrix = MMat4f()


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
        transform.pack(matrix.unsafe)
    }

    fun update(position: Vec3f, rotation: Vec3f, pivot: Vec3f = Vec3f.EMPTY, matrix: Mat4f? = null) {
        this.matrix.apply {
            clearAssign()

            translateAssign(position)
            translateAssign(pivot)
            rotateRadAssign(rotation)
            translateAssign(-pivot)
        }

        if (matrix != null) {
            Mat4Operations.times(matrix, this.matrix.unsafe, this.matrix)
        }
    }

    fun update(rotation: Vec3f, matrix: Mat4f? = null) {
        update(Vec3f.EMPTY, rotation, matrix = matrix)
    }

    fun update(position: BlockPosition, rotation: Vec3f) {
        val position = MVec3f(position - context.camera.offset.offset)
        position.x += 0.5f; position.z += 0.5f // models origin is the center of block origin
        update(position.unsafe, rotation, BLOCK_PIVOT)
    }

    private companion object {
        val BLOCK_PIVOT = Vec3f(0.0f, 0.5f, 0.0f)
    }
}
