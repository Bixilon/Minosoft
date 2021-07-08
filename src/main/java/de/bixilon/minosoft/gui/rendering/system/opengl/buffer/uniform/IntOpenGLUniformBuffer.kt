/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.gui.rendering.system.base.buffer.IntBuffer
import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL15.glBufferSubData

class IntOpenGLUniformBuffer(bindingIndex: Int = 0, override var data: IntArray = IntArray(0)) : OpenGLUniformBuffer(bindingIndex), IntBuffer {
    override val size: Int
        get() = data.size

    override fun initialUpload() {
        bind()
        glBufferData(type.gl, data, drawTypes.gl)
        unbind()
    }

    override fun upload() {
        check(initialSize == size) { "Can not change buffer size!" }
        bind()
        glBufferSubData(type.gl, 0, data)
        unbind()
    }

}
