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

package de.bixilon.minosoft.gui.rendering.chunk.block.outline

import de.bixilon.minosoft.gui.rendering.util.Mesh
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer

class BlockOutlineMesh : Mesh() {

    fun addVertex(position: Vec3) {
        data.addAll(floatArrayOf(
            position.x,
            position.y,
            position.z,
        ))
    }

    override fun load() {
        super.initializeBuffers(FLOATS_PER_VERTEX)
        var index = 0
        glVertexAttribPointer(index, 3, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.SIZE_BYTES, 0L)
        glEnableVertexAttribArray(index++)

        super.unbind()
    }


    companion object {
        private const val FLOATS_PER_VERTEX = 3
    }
}
