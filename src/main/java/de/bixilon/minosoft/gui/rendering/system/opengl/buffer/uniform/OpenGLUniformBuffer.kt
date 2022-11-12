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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.uniform

import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferDrawTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.UniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.OpenGLRenderableBuffer
import org.lwjgl.opengl.GL30.glBindBufferBase
import org.lwjgl.opengl.GL30.glBindBufferRange
import org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER

abstract class OpenGLUniformBuffer(renderSystem: OpenGLRenderSystem, override val bindingIndex: Int) : OpenGLRenderableBuffer(renderSystem, RenderableBufferTypes.UNIFORM_BUFFER), UniformBuffer {
    override val drawTypes: RenderableBufferDrawTypes = RenderableBufferDrawTypes.DYNAMIC
    protected abstract val size: Int
    protected var initialSize = -1


    override fun init() {
        super.init()
        initialUpload()
        glBindBufferRange(GL_UNIFORM_BUFFER, bindingIndex, id, 0, size.toLong())
        initialSize = size
    }


    override fun use(shader: NativeShader, bufferName: String) {
        shader.use()

        shader[bufferName] = this
        glBindBufferBase(GL_UNIFORM_BUFFER, bindingIndex, id)
    }
}
