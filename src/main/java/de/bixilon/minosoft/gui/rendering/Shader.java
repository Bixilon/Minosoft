package de.bixilon.minosoft.gui.rendering;

import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLoadingException;
import glm_.mat4x4.Mat4;
import glm_.vec3.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Shader {
    private static Shader usedShader;
    private final String vertex;
    private final String fragment;
    private int programId;

    public Shader(String vertex, String fragment) {
        this.vertex = vertex;
        this.fragment = fragment;
    }

    public int load() throws ShaderLoadingException, IOException {
        int vertexShader = ShaderUtil.createShader(this.vertex, ARBVertexShader.GL_VERTEX_SHADER_ARB);
        int fragmentShader = ShaderUtil.createShader(this.fragment, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

        this.programId = ARBShaderObjects.glCreateProgramObjectARB();

        if (this.programId == NULL) {
            throw new ShaderLoadingException();
        }

        ARBShaderObjects.glAttachObjectARB(this.programId, vertexShader);
        ARBShaderObjects.glAttachObjectARB(this.programId, fragmentShader);

        ARBShaderObjects.glLinkProgramARB(this.programId);
        if (ARBShaderObjects.glGetObjectParameteriARB(this.programId, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
            throw new ShaderLoadingException(OpenGLUtil.getLogInfo(this.programId));
        }

        ARBShaderObjects.glValidateProgramARB(this.programId);
        if (ARBShaderObjects.glGetObjectParameteriARB(this.programId, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
            throw new ShaderLoadingException(OpenGLUtil.getLogInfo(this.programId));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        return this.programId;
    }

    public int getProgramId() {
        return this.programId;
    }

    public Shader use() {
        if (usedShader != this) {
            glUseProgram(this.programId);
            usedShader = this;
        }
        return this;
    }

    public int getUniformLocation(String variableName) {
        return glGetUniformLocation(this.programId, variableName);
    }

    public void setFloat(String name, float value) {
        glUniform1f(getUniformLocation(name), value);
    }

    public void setInt(String name, int value) {
        glUniform1i(getUniformLocation(name), value);
    }

    public void set4f(String variableName, float[] floats) {
        glUniformMatrix4fv(getUniformLocation(variableName), false, floats);
    }

    public void setMat4(String variableName, Mat4 mat4) {
        glUniformMatrix4fv(getUniformLocation(variableName), false, mat4.to(BufferUtils.createFloatBuffer(16)));
    }

    public void setVec3(String name, Vec3 vec3) {
        glUniform3f(getUniformLocation(name), vec3.x, vec3.y, vec3.z);
    }
}
