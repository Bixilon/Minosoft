/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.dummy.buffer

import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import java.nio.FloatBuffer

class DummyVertexBuffer(
    override val structure: MeshStruct,
    override var buffer: FloatBuffer,
    override val type: RenderableBufferTypes = RenderableBufferTypes.ARRAY_BUFFER,
) : FloatVertexBuffer {
    override val state: RenderableBufferStates = RenderableBufferStates.PREPARING

    override fun init() {
        // TODO: memFree buffer
    }

    override fun initialUpload() {
    }

    override fun upload() {
    }

    override fun bind() {
    }

    override fun unbind() {
    }

    override fun unload() {
    }

    override val vertices: Int = 0
    override val primitiveType: PrimitiveTypes = PrimitiveTypes.QUAD

    override fun draw() {
    }
}
