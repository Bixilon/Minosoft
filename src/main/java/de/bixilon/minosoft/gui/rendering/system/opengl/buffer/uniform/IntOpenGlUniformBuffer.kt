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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.uniform

import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.IntUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import org.lwjgl.opengl.GL15.*

class IntOpenGlUniformBuffer(system: OpenGlRenderSystem, bindingIndex: Int = 0, override var data: IntArray = IntArray(0)) : OpenGlUniformBuffer(system, bindingIndex), IntUniformBuffer {
    override val size get() = data.size

    override fun initialUpload() {
        gl { glBufferData(glType, data, GL_DYNAMIC_DRAW) } // TODO: GL_STREAM_DRAW
    }

    override fun upload() {
        check(initialSize == size) { "Can not change buffer size!" }
        bind()
        gl { glBufferSubData(glType, 0, data) }
        unbind()
    }

    override fun upload(start: Int, end: Int) = TODO("Unsupported")
}
