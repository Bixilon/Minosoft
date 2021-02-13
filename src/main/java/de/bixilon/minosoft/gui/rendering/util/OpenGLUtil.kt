package de.bixilon.minosoft.gui.rendering.util

import org.lwjgl.opengl.ARBShaderObjects.*

object OpenGLUtil {
    fun getLogInfo(`object`: Int): String {
        return glGetInfoLogARB(`object`, glGetObjectParameteriARB(`object`, GL_OBJECT_INFO_LOG_LENGTH_ARB))
    }
}
