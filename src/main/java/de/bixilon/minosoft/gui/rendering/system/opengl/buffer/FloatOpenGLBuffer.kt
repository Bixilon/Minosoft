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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer

import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderFloatBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferDrawTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferTypes
import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL15.glBufferSubData
import java.nio.FloatBuffer

open class FloatOpenGLBuffer(protected var _data: FloatBuffer?) : OpenGLRenderableBuffer(RenderableBufferTypes.ARRAY_BUFFER), RenderFloatBuffer {
    override var buffer: FloatBuffer
        get() = _data!!
        set(value) {
            _data = value
        }
    override val drawTypes: RenderableBufferDrawTypes = RenderableBufferDrawTypes.STATIC

    override fun initialUpload() {
        bind()
        buffer.flip()
        glBufferData(type.gl, buffer, drawTypes.gl)
        unbind()
        state = RenderableBufferStates.UPLOADED
    }

    override fun upload() {
        bind()
        glBufferSubData(type.gl, 0, buffer)
        unbind()
    }
}
