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

package de.bixilon.minosoft.gui.rendering.models.block.state.render

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition
import de.bixilon.minosoft.gui.rendering.camera.CameraUtil
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.models.raw.display.ModelDisplay
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadConsumer.Companion.iterate
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV

class BlockGUIConsumer(
    val gui: GUIRenderer,
    val offset: Vec2f,
    val consumer: GUIVertexConsumer,
    val options: GUIVertexOptions?,
    val display: ModelDisplay,
    val size: Vec2f,
) : BlockVertexConsumer {
    private val matrix = VIEW_MATRIX * display.matrix


    @Deprecated("not chunk rendering", level = DeprecationLevel.ERROR)
    override fun addVertex(position: Vec3f, uv: Vec2f, texture: ShaderTexture, tintColor: RGBColor, lightIndex: Int) = Broken("Not chunk rendering")

    @Deprecated("not chunk rendering", level = DeprecationLevel.ERROR)
    override fun addVertex(x: Float, y: Float, z: Float, u: Float, v: Float, textureId: Float, lightTint: Float) = Broken("Not chunk rendering")

    @Deprecated("not chunk rendering", level = DeprecationLevel.ERROR)
    override fun addVertex(x: Float, y: Float, z: Float, uv: Float, textureId: Float, lightTint: Float) = Broken("Not chunk rendering")


    override fun addQuad(positions: FaceVertexData, uvData: UnpackedUV, textureId: Float, lightTint: Float) {
        val tint = RGBColor(lightTint.toBits()).rgba()

        iterate {
            val vertexOffset = it * Vec3f.LENGTH
            val uvOffset = it * Vec2f.LENGTH

            val xyz = Vec3f(positions[vertexOffset], positions[vertexOffset + 1], positions[vertexOffset + 2])

            val out = matrix * xyz

            val x = ((out.x + 0.8f) * size.x) + offset.x + 1.0f
            val y = ((-out.y + 0.81f) * size.y) + offset.y
            // values fresh from my ass

            consumer.addVertex(x, y, textureId, uvData.raw[uvOffset], uvData.raw[uvOffset + 1], tint, options)
        }
        consumer.addIndexQuad(false, true)
    }

    override fun addIndexQuad(front: Boolean, reverse: Boolean) {
        consumer.addIndexQuad(front, reverse)
    }

    companion object {
        val VIEW_MATRIX = CameraUtil.lookAt(Vec3f(0.0f, 0.0f, -1.0f), Vec3f(0.0f, 0.0f, 1.0f), CameraDefinition.CAMERA_UP_VEC3)
    }
}
