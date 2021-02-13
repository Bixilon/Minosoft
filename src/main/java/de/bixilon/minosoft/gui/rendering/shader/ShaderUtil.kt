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
