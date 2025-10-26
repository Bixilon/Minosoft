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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.vertex

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.OpenGlGpuBuffer
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.IntBuffer

class OpenGlIndexBuffer(
    system: OpenGlRenderSystem,
    private val data: IntBuffer,
) : OpenGlGpuBuffer(system) {
    override val glType get() = GL_ELEMENT_ARRAY_BUFFER


    override fun initialUpload() {
        gl { glBufferData(glType, data, GL_STATIC_DRAW) }
        this::data.forceSet(null)
    }
}
