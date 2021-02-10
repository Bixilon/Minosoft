package de.bixilon.minosoft.gui.rendering.shader;

import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLoadingException;
import de.bixilon.minosoft.gui.rendering.util.OpenGLUtil;
import de.bixilon.minosoft.util.Util;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static org.lwjgl.system.MemoryUtil.NULL;


public class ShaderUtil {
    public static int createShader(String shaderPath, int shaderType) throws ShaderLoadingException, IOException {
        int shaderId = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

        if (shaderId == NULL) {
            throw new ShaderLoadingException();
        }

        ARBShaderObjects.glShaderSourceARB(shaderId, Util.readAssetResource("/rendering/shader/" + shaderPath));
        ARBShaderObjects.glCompileShaderARB(shaderId);

        if (ARBShaderObjects.glGetObjectParameteriARB(shaderId, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
            throw new ShaderLoadingException(OpenGLUtil.getLogInfo(shaderId));
        }

        return shaderId;
    }
}
