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

package de.bixilon.minosoft.gui.rendering.system.opengl

import org.lwjgl.opengl.ARBUniformBufferObject.GL_UNIFORM_BUFFER
import org.lwjgl.opengl.GL15.*

class FloatUniformBuffer(bindingIndex: Int = 0, var data: FloatArray = FloatArray(0)) : UniformBuffer(bindingIndex) {
    override val size: Int
        get() = data.size

    override fun initialUpload() {
        bind()
        glBufferData(GL_UNIFORM_BUFFER, data, GL_DYNAMIC_DRAW)
        unbind()
    }

    override fun upload() {
        check(initialSize == size) { "Can not change buffer size!" }
        bind()
        glBufferSubData(GL_UNIFORM_BUFFER, 0, data)
        unbind()
    }

}
