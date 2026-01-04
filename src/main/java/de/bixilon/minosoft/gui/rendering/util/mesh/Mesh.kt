/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.util.mesh

import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.system.base.buffer.GpuBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.VertexBuffer

open class Mesh(
    val buffer: VertexBuffer,
) : Drawable {
    var state = MeshStates.PREPARING
        private set

    open fun load() {
        assert(state == MeshStates.PREPARING) { "Invalid mesh state: $state" }
        if (buffer.state != GpuBufferStates.INITIALIZED) {
            // buffer might have been loaded async
            buffer.init()
        }
        state = MeshStates.LOADED
    }

    open fun unload() {
        assert(state == MeshStates.LOADED) { "Invalid mesh state: $state" }
        buffer.unload()
        state = MeshStates.UNLOADED
    }

    override fun draw() {
        assert(state == MeshStates.LOADED) { "Invalid mesh state: $state" }
        return buffer.draw()
    }

    fun drop() {
        assert(state == MeshStates.PREPARING) { "Invalid mesh state: $state" }
        buffer.drop()
        state = MeshStates.UNLOADED
    }
}
