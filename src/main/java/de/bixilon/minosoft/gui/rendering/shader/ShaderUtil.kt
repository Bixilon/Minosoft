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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLoadingException
import de.bixilon.minosoft.gui.rendering.util.OpenGLUtil
import org.lwjgl.opengl.ARBShaderObjects.*
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.system.MemoryUtil

object ShaderUtil {
    fun createShader(assetsManager: AssetsManager = Minosoft.MINOSOFT_ASSETS_MANAGER, resourceLocation: ResourceLocation, shaderType: Int): Int {
        val shaderId = glCreateShaderObjectARB(shaderType)
        if (shaderId.toLong() == MemoryUtil.NULL) {
            throw ShaderLoadingException()
        }

        glShaderSourceARB(shaderId, assetsManager.readStringAsset(resourceLocation))
        glCompileShaderARB(shaderId)

        if (glGetObjectParameteriARB(shaderId, GL_OBJECT_COMPILE_STATUS_ARB) == GL_FALSE) {
            throw ShaderLoadingException(OpenGLUtil.getLogInfo(shaderId))
        }

        return shaderId
    }
}
