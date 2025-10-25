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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.config.DebugOptions.EMPTY_BUFFERS
import de.bixilon.minosoft.gui.rendering.system.base.buffer.type.FloatGpuBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.GL_STATIC_DRAW
import org.lwjgl.opengl.GL15C
import org.lwjgl.system.MemoryUtil.memAddress0
import java.nio.FloatBuffer

class FloatOpenGlBuffer(system: OpenGlRenderSystem, override val data: FloatBuffer) : OpenGlGpuBuffer(system), FloatGpuBuffer {

    override val glType get() = GL_ARRAY_BUFFER


    override fun initialUpload() {
        val position = data.position()
        gl { nglBufferData(glType, data, if (EMPTY_BUFFERS) 0 else position, GL_STATIC_DRAW) }
        this::data.forceSet(null)
    }


    private fun nglBufferData(target: Int, buffer: FloatBuffer, length: Int, usage: Int) {
        gl { GL15C.nglBufferData(target, Integer.toUnsignedLong(length) shl 2, memAddress0(buffer), usage) }
    }
}
