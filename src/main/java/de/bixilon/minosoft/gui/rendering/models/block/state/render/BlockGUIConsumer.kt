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
import de.bixilon.minosoft.gui.rendering.models.raw.display.ModelDisplay
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture

class BlockGUIConsumer(
    val gui: GUIRenderer,
    val offset: Vec2,
    val consumer: GUIVertexConsumer,
    val options: GUIVertexOptions?,
    val display: ModelDisplay,
    val size: Vec2,
) : BlockVertexConsumer {
    private val matrix = VIEW_MATRIX * display.matrix
    override val order = consumer.order


    override fun addVertex(position: FloatArray, uv: Vec2, texture: ShaderTexture, tintColor: Int, light: Int) = Broken("Not chunk rendering")
    override fun addVertex(x: Float, y: Float, z: Float, u: Float, v: Float, textureId: Float, lightTint: Float) = Broken("Not chunk rendering")
    override fun addQuad(offset: FloatArray, positions: FaceVertexData, uvData: FaceVertexData, textureId: Float, lightTint: Float) = Broken("Not chunk rendering")

    override fun addQuad(positions: FaceVertexData, uvData: FaceVertexData, textureId: Float, lightTint: Float) {

        val tint = (lightTint.toBits() shl 8) or 0xFF


        gui.context.system.quadOrder.iterateReverse { p, uv ->
            val vertexOffset = p * Vec3.length
            val uvOffset = uv * Vec2.length

            val xyz = Vec4(positions[vertexOffset], positions[vertexOffset + 1], positions[vertexOffset + 2], 1.0f)

            val out = matrix * xyz

            val x = ((out.x + 0.8f) * size.x) + offset.x + 1.0f
            val y = ((-out.y + 0.81f) * size.y) + offset.y
            // values fresh from my ass

            consumer.addVertex(x, y, textureId, uvData[uvOffset], uvData[uvOffset + 1], tint, options)
        }

        // block renders from (in normal cases) from 0 to 1
        // matrix should map those pixels into screen 2d space (offset until offset+size)
    }

    companion object {
        val VIEW_MATRIX = GLM.lookAt(Vec3(0.0f, 0.0f, -1.0f), Vec3(0.0f, 0.0f, 1.0f), CameraDefinition.CAMERA_UP_VEC3)
    }
}
