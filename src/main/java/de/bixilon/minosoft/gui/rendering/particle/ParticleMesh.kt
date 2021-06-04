/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray
import org.lwjgl.opengl.GL15.GL_POINTS
import org.lwjgl.opengl.GL15.glDrawArrays

class ParticleMesh : Mesh(ParticleMeshStruct::class) {

    fun addVertex(position: Vec3, scale: Float, texture: Texture, tintColor: RGBColor, uvMin: Vec2 = Vec2(0, 0), uvMax: Vec2 = Vec2(1, 1)) {
        val textureLayer = if (RenderConstants.FORCE_DEBUG_TEXTURE) {
            RenderConstants.DEBUG_TEXTURE_ID
        } else {
            (texture.arrayId shl 24) or texture.arrayLayer
        }

        data.addAll(floatArrayOf(
            position.x,
            position.y,
            position.z,
            uvMin.x,
            uvMin.y,
            uvMax.x * texture.uvEnd.x,
            uvMax.y * texture.uvEnd.y,
            Float.fromBits(textureLayer),
            Float.fromBits(texture.properties.animation?.animationId ?: -1),
            scale,
            Float.fromBits(tintColor.rgba),
        ))
    }


    override fun draw() {
        glBindVertexArray(vao)
        glDrawArrays(GL_POINTS, 0, primitiveCount)
    }


    data class ParticleMeshStruct(
        val position: Vec3,
        val minUVCoordinates: Vec2,
        val maxUVCoordinates: Vec2,
        val textureLayer: Int,
        val animationId: Int,
        val scale: Float,
        val tintColor: RGBColor,
    ) {
        companion object : MeshStruct(ParticleMeshStruct::class)
    }
}
