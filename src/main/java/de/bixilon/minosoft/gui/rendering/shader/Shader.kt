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

package de.bixilon.minosoft.gui.rendering.shader

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.commands.CommandStringReader
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLoadingException
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.gui.rendering.util.OpenGLUtil
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.MMath
import glm_.mat4x4.Mat4
import glm_.mat4x4.Mat4d
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
import java.io.FileNotFoundException

class Shader(
    private val renderWindow: RenderWindow,
    private val resourceLocation: ResourceLocation,
    private val defines: Map<String, Any> = mapOf(),
) {
    lateinit var uniforms: List<String>
        private set
    private var programId = 0

    fun load(assetsManager: AssetsManager = Minosoft.MINOSOFT_ASSETS_MANAGER): Int {
        val uniforms: MutableList<String> = mutableListOf()
        val pathPrefix = "${resourceLocation.namespace}:rendering/shader/${resourceLocation.path}/${resourceLocation.path.replace("/", "_")}"
        val vertexShader = createShader(assetsManager, renderWindow, ResourceLocation("$pathPrefix.vsh"), GL_VERTEX_SHADER_ARB, defines, uniforms)!!
        val geometryShader = createShader(assetsManager, renderWindow, ResourceLocation("$pathPrefix.gsh"), GL_GEOMETRY_SHADER_ARB, defines, uniforms)
        val fragmentShader = createShader(assetsManager, renderWindow, ResourceLocation("$pathPrefix.fsh"), GL_FRAGMENT_SHADER_ARB, defines, uniforms)!!
        this.uniforms = uniforms.toList()
        programId = glCreateProgramObjectARB()

        if (programId.toLong() == MemoryUtil.NULL) {
            throw ShaderLoadingException()
        }

        glAttachObjectARB(programId, vertexShader)
        geometryShader?.let {
            glAttachObjectARB(programId, it)
        }
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
        geometryShader?.let {
            glDeleteShader(it)
        }
        glDeleteShader(fragmentShader)

        val context = Rendering.currentContext!!
        context.shaders.add(this)
        context.renderSystem.unsafeCast<OpenGLRenderSystem>().shaders[this] = programId
        return programId
    }

    fun use(): Shader {
        renderWindow.renderSystem.shader = this
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

    fun setMat4(uniformName: String, mat4: Mat4d) {
        setMat4(uniformName, Mat4(mat4))
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

    fun setRGBColor(uniformName: String, color: RGBColor) {
        setVec4(uniformName, Vec4(color.floatRed, color.floatGreen, color.floatBlue, color.alpha))
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
        }
    }

    fun setTexture(uniformName: String, textureId: Int) {
        glUniform1i(getUniformLocation(uniformName), textureId)
    }

    fun setUniformBuffer(uniformName: String, bindingIndex: Int) {
        glUniformBlockBinding(programId, glGetUniformBlockIndex(programId, uniformName), bindingIndex)
    }

    fun getLog(): String {
        return glGetShaderInfoLog(programId)
    }


    companion object {
        private val DEFAULT_DEFINES: Map<String, (renderWindow: RenderWindow) -> Any?> = mapOf(
            "ANIMATED_TEXTURE_COUNT" to {
                MMath.clamp(it.textures.animator.animatedTextures.size, 1, TextureArray.MAX_ANIMATED_TEXTURES)
            }
        )

        private fun createShader(assetsManager: AssetsManager = Minosoft.MINOSOFT_ASSETS_MANAGER, renderWindow: RenderWindow, resourceLocation: ResourceLocation, shaderType: Int, defines: Map<String, Any>, uniforms: MutableList<String>): Int? {
            val shaderId = glCreateShaderObjectARB(shaderType)
            if (shaderId.toLong() == MemoryUtil.NULL) {
                throw ShaderLoadingException()
            }
            val total = StringBuilder()
            val lines = try {
                assetsManager.readStringAsset(resourceLocation).lines()
            } catch (exception: FileNotFoundException) {
                return null
            }

            for (line in lines) {
                val reader = CommandStringReader(line)
                when {
                    line.startsWith("#include ") -> {
                        val includeResourceLocation = ResourceLocation(line.removePrefix("#include ").removePrefix("\"").removeSuffix("\"").replace("\\\"", "\""))
                        total.append("\n")
                        total.append(assetsManager.readStringAsset(if (includeResourceLocation.path.contains(".glsl")) {
                            includeResourceLocation
                        } else {
                            ResourceLocation(includeResourceLocation.namespace, "rendering/shader/includes/${includeResourceLocation.path}.glsl")
                        }))

                        total.append("\n")
                        continue
                    }
                }

                total.append(line)
                total.append('\n')


                fun pushDefine(name: String, value: Any) {
                    total.append("#define ")
                    total.append(name)
                    total.append(' ')
                    total.append(value)
                    total.append('\n')
                }

                when {
                    line.startsWith("#version") -> {
                        // add all defines
                        total.append('\n')
                        for ((name, value) in defines) {
                            pushDefine(name, value)
                        }

                        for ((name, value) in DEFAULT_DEFINES) {
                            value(renderWindow)?.let { pushDefine(name, it) }
                        }

                        // ToDo: Don't do that!
                        if (renderWindow.renderSystem is OpenGLRenderSystem) {
                            renderWindow.renderSystem.vendor.define?.let { pushDefine(it, "") }
                        }
                    }
                    line.startsWith("uniform ") -> { // ToDo: Packed in layout
                        reader.readUnquotedString() // "uniform"
                        reader.skipWhitespaces()
                        reader.readUnquotedString() // datatype
                        reader.skipWhitespaces()
                        uniforms.add(reader.readString()) // uniform name
                    }
                }
            }

            glShaderSourceARB(shaderId, total.toString())
            glCompileShaderARB(shaderId)

            if (glGetObjectParameteriARB(shaderId, GL_OBJECT_COMPILE_STATUS_ARB) == GL_FALSE) {
                throw ShaderLoadingException(OpenGLUtil.getLogInfo(shaderId))
            }

            return shaderId
        }
    }
}
