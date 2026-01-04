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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.vertex

import de.bixilon.kutil.profiler.stack.StackedProfiler.Companion.invoke
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.config.DebugOptions.EMPTY_BUFFERS
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.OpenGlGpuBuffer
import de.bixilon.minosoft.util.collections.ints.IntListUtil.copy
import org.lwjgl.opengl.GL15C.nglBufferSubData
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.IntBuffer

class OpenGlIndexBuffer(
    system: OpenGlRenderSystem,
    val data: IntBuffer,
    val free: Boolean,
) : OpenGlGpuBuffer(system) {
    override val glType get() = GL_ELEMENT_ARRAY_BUFFER


    private fun uploadMapped(size: Int): Boolean {
        val mapped = gl { glMapBuffer(glType, GL_WRITE_ONLY) }?.asIntBuffer() ?: return false
        Rendering.currentContext?.profiler.invoke("copy") { data.copy(0, mapped, 0, size) }
        assert(gl { glUnmapBuffer(glType) })

        return true
    }

    override fun initialUpload() {
        val size = if (EMPTY_BUFFERS) 0 else data.limit()
        gl { glBufferData(glType, Integer.toUnsignedLong(size) * Int.SIZE_BYTES, GL_STATIC_DRAW) }

        if (!uploadMapped(size)) {
            gl { nglBufferSubData(glType, data, size) }
        }
        unsafeDrop()
    }


    override fun unsafeDrop() {
        if (free && data.isDirect) memFree(this.data)
        this::data.forceSet(null)
    }

    private fun nglBufferSubData(target: Int, buffer: IntBuffer, size: Int) {
        if (buffer.isDirect) {
            gl { nglBufferSubData(target, 0L, Integer.toUnsignedLong(size) * Int.SIZE_BYTES, MemoryUtil.memAddress0(buffer)) }
        } else {
            val array = if (size == 0) IntArray(0) else buffer.array() // TODO: support length
            gl { glBufferSubData(target, 0L, array) }
        }
    }
}
