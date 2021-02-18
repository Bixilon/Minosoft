/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.shader

import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLoadingException
import de.bixilon.minosoft.gui.rendering.util.OpenGLUtil
import de.bixilon.minosoft.util.Util
import org.lwjgl.opengl.ARBShaderObjects.*
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.system.MemoryUtil

object ShaderUtil {
    fun createShader(shaderPath: String, shaderType: Int): Int {
        val shaderId = glCreateShaderObjectARB(shaderType)
        if (shaderId.toLong() == MemoryUtil.NULL) {
            throw ShaderLoadingException()
        }

        glShaderSourceARB(shaderId, Util.readAssetResource("rendering/shader/$shaderPath"))
        glCompileShaderARB(shaderId)

        if (glGetObjectParameteriARB(shaderId, GL_OBJECT_COMPILE_STATUS_ARB) == GL_FALSE) {
            throw ShaderLoadingException(OpenGLUtil.getLogInfo(shaderId))
        }

        return shaderId
    }
}
