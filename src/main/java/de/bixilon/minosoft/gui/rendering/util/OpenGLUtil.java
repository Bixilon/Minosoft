package de.bixilon.minosoft.gui.rendering.util;

import org.lwjgl.opengl.ARBShaderObjects;

public final class OpenGLUtil {
    public static String getLogInfo(int obj) {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }
}
