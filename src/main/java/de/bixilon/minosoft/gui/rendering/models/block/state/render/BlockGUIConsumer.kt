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

package de.bixilon.minosoft.gui.rendering.models.block.state.render

import de.bixilon.kotlinglm.GLM
import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture

class BlockGUIConsumer(
    val gui: GUIRenderer,
    val offset: Vec2,
    val consumer: GUIVertexConsumer,
    val options: GUIVertexOptions?,
    val size: Vec2,
) : BlockVertexConsumer {
    override val order = consumer.order


    override fun addVertex(position: FloatArray, uv: Vec2, texture: ShaderTexture, tintColor: Int, light: Int) = Broken("Not chunk rendering")
    override fun addVertex(x: Float, y: Float, z: Float, u: Float, v: Float, textureId: Float, lightTint: Float) = Broken("Not chunk rendering")
    override fun addQuad(offset: FloatArray, positions: FaceVertexData, uvData: FaceVertexData, textureId: Float, lightTint: Float) = Broken("Not chunk rendering")

    override fun addQuad(positions: FaceVertexData, uvData: FaceVertexData, textureId: Float, lightTint: Float) {
        val position = Vec3(0, 0, -1) // one block offset in north direction
        val front = Vec3(0, 0, 1) // and directly looking onto the south side
        // TODO: look from front (whatever that means) to the block in 45° angle from above
        val view = GLM.lookAt(position, position + front, CameraDefinition.CAMERA_UP_VEC3)
        val projection = GLM.perspective(45.0f.rad, size.x / size.y, CameraDefinition.NEAR_PLANE, CameraDefinition.FAR_PLANE)

        val viewProjection = view * projection

        val tint = (lightTint.toBits() shl 8) or 0xFF

        order.iterate { p, uv ->
            val vertexOffset = p * Vec3.length
            val uvOffset = uv * Vec2.length

            val xyz = Vec4(positions[vertexOffset], positions[vertexOffset + 1], positions[vertexOffset + 2], 1.0f)

            val a = viewProjection * xyz

            var x = (a.x * 0.5f) + 0.5f
            x = (x * size.x) + offset.x
            var y = (a.y * 0.5f) + 0.5f
            y = (y * size.y) + offset.y

            consumer.addVertex(x, y, textureId, uvData[uvOffset], uvData[uvOffset + 1], tint, options)
        }

        // block renders from (in normal cases) from 0 to 1
        // matrix should map those pixels into screen 2d space (offset until offset+size)
    }
}
