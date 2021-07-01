/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLoadingException
import de.bixilon.minosoft.gui.rendering.system.base.shader.GLSLUtil
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.util.OpenGLUtil
import de.bixilon.minosoft.util.KUtil.unsafeCast
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.ARBFragmentShader.GL_FRAGMENT_SHADER_ARB
import org.lwjgl.opengl.ARBGeometryShader4.GL_GEOMETRY_SHADER_ARB
import org.lwjgl.opengl.ARBShaderObjects.*
import org.lwjgl.opengl.ARBVertexShader.GL_VERTEX_SHADER_ARB
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL43.*
import org.lwjgl.system.MemoryUtil

class OpenGLShader(
    override val renderWindow: RenderWindow,
    override val resourceLocation: ResourceLocation,
    private val defines: Map<String, Any> = mapOf(),
) : Shader {
    private var program = -1
    override lateinit var uniforms: List<String>
        private set


    private fun load(
        assetsManager: AssetsManager = Minosoft.MINOSOFT_ASSETS_MANAGER,
        resourceLocation: ResourceLocation,
        shaderType: Int,
        defines: Map<String, Any>,
        uniforms: MutableList<String>
    ): Int? {
        val program = glCreateShaderObjectARB(shaderType)
        if (program.toLong() == MemoryUtil.NULL) {
            throw ShaderLoadingException()
        }
        val code = GLSLUtil.readGLSL(assetsManager, renderWindow, resourceLocation, defines, uniforms)

        glShaderSourceARB(program, code)
        glCompileShaderARB(program)

        if (glGetObjectParameteriARB(program, GL_OBJECT_COMPILE_STATUS_ARB) == GL_FALSE) {
            throw ShaderLoadingException(OpenGLUtil.getLogInfo(program))
        }

        return program
    }

    override fun load(assetsManager: AssetsManager) {
        val uniforms: MutableList<String> = mutableListOf()
        val pathPrefix = "${resourceLocation.namespace}:rendering/shader/${resourceLocation.path}/${
            resourceLocation.path.replace(
                "/",
                "_"
            )
        }"
        val vertexShader =
            load(assetsManager, ResourceLocation("$pathPrefix.vsh"), GL_VERTEX_SHADER_ARB, defines, uniforms)!!
        val geometryShader =
            load(assetsManager, ResourceLocation("$pathPrefix.gsh"), GL_GEOMETRY_SHADER_ARB, defines, uniforms)
        val fragmentShader =
            load(assetsManager, ResourceLocation("$pathPrefix.fsh"), GL_FRAGMENT_SHADER_ARB, defines, uniforms)!!
        this.uniforms = uniforms.toList()

        program = glCreateProgramObjectARB()

        if (program.toLong() == MemoryUtil.NULL) {
            throw ShaderLoadingException()
        }

        glAttachObjectARB(program, vertexShader)
        geometryShader?.let {
            glAttachObjectARB(program, it)
        }
        glAttachObjectARB(program, fragmentShader)
        glLinkProgramARB(program)

        if (glGetObjectParameteriARB(program, GL_OBJECT_LINK_STATUS_ARB) == GL_FALSE) {
            throw ShaderLoadingException(OpenGLUtil.getLogInfo(program))
        }

        glValidateProgramARB(program)

        if (glGetObjectParameteriARB(program, GL_OBJECT_VALIDATE_STATUS_ARB) == GL_FALSE) {
            throw ShaderLoadingException(OpenGLUtil.getLogInfo(program))
        }
        glDeleteShader(vertexShader)
        geometryShader?.let {
            glDeleteShader(it)
        }
        glDeleteShader(fragmentShader)

        val context = Rendering.currentContext!!
        context.shaders.add(this)
        context.renderSystem.unsafeCast<OpenGLRenderSystem>().shaders[this] = program
    }

    private fun getUniformLocation(uniformName: String): Int {
        return glGetUniformLocation(program, uniformName)
    }

    override fun setFloat(uniformName: String, value: Float) {
        glUniform1f(getUniformLocation(uniformName), value)
    }

    override fun setInt(uniformName: String, value: Int) {
        glUniform1i(getUniformLocation(uniformName), value)
    }

    override fun setMat4(uniformName: String, mat4: Mat4) {
        glUniformMatrix4fv(getUniformLocation(uniformName), false, mat4 to BufferUtils.createFloatBuffer(16))
    }

    override fun setVec2(uniformName: String, vec2: Vec2) {
        glUniform2f(getUniformLocation(uniformName), vec2.x, vec2.y)
    }

    override fun setVec3(uniformName: String, vec3: Vec3) {
        glUniform3f(getUniformLocation(uniformName), vec3.x, vec3.y, vec3.z)
    }

    override fun setVec4(uniformName: String, vec4: Vec4) {
        glUniform4f(getUniformLocation(uniformName), vec4.x, vec4.y, vec4.z, vec4.w)
    }

    override fun setArray(uniformName: String, array: Array<*>) {
        for ((i, value) in array.withIndex()) {
            val currentUniformName = "$uniformName[$i]"
            this[currentUniformName] = value
        }
    }

    override fun setRGBColor(uniformName: String, color: RGBColor) {
        setVec4(uniformName, Vec4(color.floatRed, color.floatGreen, color.floatBlue, color.alpha))
    }

    override fun setTexture(uniformName: String, textureId: Int) {
        glUniform1i(getUniformLocation(uniformName), textureId)
    }

    override fun setUniformBuffer(uniformName: String, uniformBuffer: UniformBuffer) {
        glUniformBlockBinding(program, glGetUniformBlockIndex(program, uniformName), uniformBuffer.bindingIndex)
    }

    override val log: String
        get() = TODO()
}
