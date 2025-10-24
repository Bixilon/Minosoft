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

package de.bixilon.minosoft.gui.rendering.system.dummy.buffer

import de.bixilon.minosoft.gui.rendering.system.base.buffer.GpuBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import java.nio.FloatBuffer

class DummyVertexBuffer(
    override val struct: MeshStruct,
    override var buffer: FloatBuffer,
) : FloatVertexBuffer {
    override val state: GpuBufferStates = GpuBufferStates.PREPARING

    override fun init() {
        // TODO: memFree buffer
    }

    override fun upload() {
    }

    override fun unload() {
    }

    override val vertices: Int = 0
    override val primitive: PrimitiveTypes = PrimitiveTypes.QUAD

    override fun draw() {
    }
}
