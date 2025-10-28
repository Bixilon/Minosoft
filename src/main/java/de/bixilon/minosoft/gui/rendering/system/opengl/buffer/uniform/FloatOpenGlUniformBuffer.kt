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

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.FloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL15C.nglBufferSubData
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.FloatBuffer

class FloatOpenGlUniformBuffer(
    system: OpenGlRenderSystem,
    bindingIndex: Int,
    override var data: FloatBuffer,
) : OpenGlUniformBuffer(system, bindingIndex), FloatUniformBuffer {
    override val size get() = data.limit()

    override fun initialUpload() {
        data.position(0)
        gl { glBufferData(glType, data, GL_STREAM_DRAW) }
    }

    override fun upload() {
        check(initialSize == size) { "Can not change buffer size!" }
        bind()
        gl { glBufferSubData(glType, 0, data) }
        unbind()
    }

    override fun upload(start: Int, end: Int) {
        check(initialSize == size) { "Can not change buffer size!" }
        if (start < 0 || end >= size) {
            throw IndexOutOfBoundsException(start)
        }
        bind()
        gl { nglBufferSubData(glType, start.toLong() * Float.SIZE_BYTES, Integer.toUnsignedLong(((end + 1) - start) * Float.SIZE_BYTES), MemoryUtil.memAddress(data, start)) }
        unbind()
    }

    override fun unsafeDrop() {
        if (data.isDirect) memFree(data)
        this::data.forceSet(null)
    }
}
