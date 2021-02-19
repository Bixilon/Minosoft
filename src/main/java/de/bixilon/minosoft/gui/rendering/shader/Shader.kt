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
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.gui.rendering.util.OpenGLUtil
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.ARBFragmentShader.GL_FRAGMENT_SHADER_ARB
import org.lwjgl.opengl.ARBShaderObjects.*
import org.lwjgl.opengl.ARBVertexShader.GL_VERTEX_SHADER_ARB
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryUtil

class Shader(private val vertexPath: String, private val fragmentPath: String) {
    private var programId = 0

    fun load(): Int {
        val vertexShader = ShaderUtil.createShader(vertexPath, GL_VERTEX_SHADER_ARB)
        val fragmentShader = ShaderUtil.createShader(fragmentPath, GL_FRAGMENT_SHADER_ARB)
        programId = glCreateProgramObjectARB()

        if (programId.toLong() == MemoryUtil.NULL) {
            throw ShaderLoadingException()
        }

        glAttachObjectARB(programId, vertexShader)
        glAttachObjectARB(programId, fragmentShader)
        glLinkProgramARB(programId)

        if (glGetObjectParameteriARB(programId, GL_OBJECT_LINK_STATUS_ARB) == GL_FALSE) {
            throw ShaderLoadingException(OpenGLUtil.getLogInfo(programId))
        }

        glValidateProgramARB(programId)

        if (glGetObjectParameteriARB(programId, GL_OBJECT_VALIDATE_STATUS_ARB) == GL_FALSE) {
            throw ShaderLoadingException(OpenGLUtil.getLogInfo(programId))
        }
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)

        return programId
    }

    fun use(): Shader {
        if (usedShader !== this) {
            glUseProgram(programId)
            usedShader = this
        }
        return this
    }

    private fun getUniformLocation(uniformName: String): Int {
        return glGetUniformLocation(programId, uniformName)
    }

    fun setFloat(uniformName: String, value: Float) {
        glUniform1f(getUniformLocation(uniformName), value)
    }

    fun setInt(uniformName: String, value: Int) {
        glUniform1i(getUniformLocation(uniformName), value)
    }

    fun setMat4(uniformName: String, mat4: Mat4) {
        glUniformMatrix4fv(getUniformLocation(uniformName), false, mat4 to BufferUtils.createFloatBuffer(16))
    }

    fun setVec2(uniformName: String, vec2: Vec2) {
        glUniform2f(getUniformLocation(uniformName), vec2.x, vec2.y)
    }

    fun setVec3(uniformName: String, vec3: Vec3) {
        glUniform3f(getUniformLocation(uniformName), vec3.x, vec3.y, vec3.z)
    }

    fun setVec4(uniformName: String, vec4: Vec4) {
        glUniform4f(getUniformLocation(uniformName), vec4.x, vec4.y, vec4.z, vec4.w)
    }

    fun setArray(uniformName: String, array: Array<*>) {
        for ((i, value) in array.withIndex()) {
            val currentUniformName = "$uniformName[$i]"
            setUniform(currentUniformName, value)
        }
    }

    fun setUniform(uniformName: String, data: Any?) {
        if (data == null) {
            return
        }
        when (data) {
            is Array<*> -> setArray(uniformName, data)
            is Int -> setInt(uniformName, data)
            is Float -> setFloat(uniformName, data)
            is Mat4 -> setMat4(uniformName, data)
            is Vec4 -> setVec4(uniformName, data)
            is Vec3 -> setVec3(uniformName, data)
            is Vec2 -> setVec2(uniformName, data)
            is TextureArray -> setTexture(uniformName, data)
        }

    }

    fun setTexture(uniformName: String, textureArray: TextureArray) {
        glUniform1i(getUniformLocation(uniformName), textureArray.textureId - 1)
    }


    companion object {
        private var usedShader: Shader? = null
    }
}
