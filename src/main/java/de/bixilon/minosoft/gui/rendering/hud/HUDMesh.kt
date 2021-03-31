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

package de.bixilon.minosoft.gui.rendering.hud

import de.bixilon.minosoft.gui.rendering.util.Mesh
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer

class HUDMesh : Mesh() {

    override fun load() {
        super.initializeBuffers(FLOATS_PER_VERTEX)
        var index = 0
        glVertexAttribPointer(index, 3, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.SIZE_BYTES, 0L)
        glEnableVertexAttribArray(index++)
        glVertexAttribPointer(index, 2, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.SIZE_BYTES, (3 * Float.SIZE_BYTES).toLong())
        glEnableVertexAttribArray(index++)
        glVertexAttribPointer(index, 1, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.SIZE_BYTES, (5 * Float.SIZE_BYTES).toLong())
        glEnableVertexAttribArray(index++)
        glVertexAttribPointer(index, 1, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.SIZE_BYTES, (6 * Float.SIZE_BYTES).toLong())
        glEnableVertexAttribArray(index++)
        super.unbind()
    }

    fun addCacheMesh(cacheMesh: HUDCacheMesh) {
        data!!.addAll(cacheMesh.cache)
    }


    companion object {
        const val FLOATS_PER_VERTEX = 7
    }
}
