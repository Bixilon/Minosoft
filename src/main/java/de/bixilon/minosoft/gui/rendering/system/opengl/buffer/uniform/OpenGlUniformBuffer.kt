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

import de.bixilon.minosoft.gui.rendering.shader.AbstractShader
import de.bixilon.minosoft.gui.rendering.system.base.buffer.GpuBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.UniformBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.OpenGlGpuBuffer
import org.lwjgl.opengl.GL30.glBindBufferBase
import org.lwjgl.opengl.GL30.glBindBufferRange
import org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER

abstract class OpenGlUniformBuffer(system: OpenGlRenderSystem, override val bindingIndex: Int) : OpenGlGpuBuffer(system), UniformBuffer {
    protected abstract val size: Int
    protected var initialSize = -1

    override val glType get() = GL_UNIFORM_BUFFER


    override fun init() {
        super.init()
        gl { glBindBufferRange(glType, bindingIndex, id, 0, size.toLong()) }
        initialSize = size
    }


    override fun use(shader: AbstractShader, bufferName: String) {
        if (this.state != GpuBufferStates.INITIALIZED) throw IllegalStateException("Not uploaded (buffer=$this, state=$state)")
        shader.use()

        shader.native[bufferName] = this
        gl { glBindBufferBase(glType, bindingIndex, id) }
    }
}
