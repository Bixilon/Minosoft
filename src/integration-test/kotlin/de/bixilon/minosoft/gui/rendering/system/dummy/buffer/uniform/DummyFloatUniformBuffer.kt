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

package de.bixilon.minosoft.gui.rendering.system.dummy.buffer.uniform

import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.FloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import java.nio.FloatBuffer

class DummyFloatUniformBuffer(
    override var buffer: FloatBuffer,
) : FloatUniformBuffer {
    override val bindingIndex: Int = 0

    override fun upload(start: Int, end: Int) {
    }

    override fun upload() {
    }

    override fun use(shader: NativeShader, bufferName: String) {
    }

    override val state: RenderableBufferStates = RenderableBufferStates.UPLOADED
    override val type: RenderableBufferTypes = RenderableBufferTypes.UNIFORM_BUFFER

    override fun init() {
    }

    override fun initialUpload() {
    }

    override fun bind() {
    }

    override fun unbind() {
    }

    override fun unload() {
    }
}
