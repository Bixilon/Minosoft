/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.util.Mesh
import glm_.BYTES
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer

class SectionArrayMesh : Mesh() {
    var lowestBlockHeight = 0
    var highestBlockHeight = 0

    fun addVertex(position: Vec3, textureCoordinates: Vec2, texture: Texture, tintColor: RGBColor?, lightLevel: Int = 14) {
        val data = data!!
        data.add(position.x)
        data.add(position.y)
        data.add(position.z)
        data.add(textureCoordinates.x * texture.uvEnd.x)
        data.add(textureCoordinates.y * texture.uvEnd.y)
        data.add(Float.fromBits((texture.arrayId shl 24) or texture.arrayLayer))

        val color = tintColor ?: ChatColors.WHITE

        val lightFactor = MAX_LIGHT_LEVEL / (lightLevel + 1)

        val lightColor = RGBColor(color.red / lightFactor, color.green / lightFactor, color.blue / lightFactor)

        data.add(Float.fromBits(lightColor.color ushr 8))
    }

    override fun load() {
        super.initializeBuffers(FLOATS_PER_VERTEX)
        var index = 0
        glVertexAttribPointer(index, 3, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, 0L)
        glEnableVertexAttribArray(index++)

        glVertexAttribPointer(index, 2, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, (3 * Float.BYTES).toLong())
        glEnableVertexAttribArray(index++)

        glVertexAttribPointer(index, 1, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, (5 * Float.BYTES).toLong())
        glEnableVertexAttribArray(index++)

        glVertexAttribPointer(index, 1, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, (6 * Float.BYTES).toLong())
        glEnableVertexAttribArray(index++)

        super.unbind()
    }


    companion object {
        private const val FLOATS_PER_VERTEX = 7
        private const val MAX_LIGHT_LEVEL = 17
        private const val MAX_LIGHT_LEVEL_FLOAT = 17f // Level 0 and 15 kind of does not exist here.
    }
}
