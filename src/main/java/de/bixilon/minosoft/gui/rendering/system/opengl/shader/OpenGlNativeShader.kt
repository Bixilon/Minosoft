/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.opengl.shader

import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLinkingException
import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLoadingException
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.UniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.code.glsl.GLSLShaderCode
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.uniform.OpenGlUniformBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL43.*
import org.lwjgl.system.MemoryUtil
import java.io.FileNotFoundException

class OpenGlNativeShader(
    val system: OpenGlRenderSystem,
    private val vertex: ResourceLocation,
    private val geometry: ResourceLocation?,
    private val fragment: ResourceLocation,
) : NativeShader {
    override val context get() = system.context
    override var loaded: Boolean = false
        private set
    override val defines: MutableMap<String, Any> = mutableMapOf()
    private var handler = -1
    private val uniformLocations: Object2IntOpenHashMap<String> = Object2IntOpenHashMap()

    private fun load(file: ResourceLocation, type: ShaderType, code: String?): Int {
        val code = GLSLShaderCode(context, code ?: context.session.assetsManager[file].readAsString())
        system.log { "Compiling shader $file" }

        code.defines += defines
        code.defines["SHADER_TYPE_${type.name}"] = ""
        for (hack in system.vendor.hacks) {
            code.defines[hack.name] = ""
        }

        val program = gl { glCreateShader(type.native) }
        if (program.toLong() == MemoryUtil.NULL) {
            throw ShaderLoadingException()
        }

        val glsl = code.code
        gl { glShaderSource(program, glsl) }

        gl { glCompileShader(program) }

        if (gl { glGetShaderi(program, GL_COMPILE_STATUS) } == GL_FALSE) {
            throw ShaderLoadingException("Can not load shader: $file:\n" + gl { glGetShaderInfoLog(program) }, glsl)
        }

        return program
    }

    override fun load() {
        val geometryCode = geometry?.let { catchAll { context.session.assetsManager[it].readAsString() } }
        if (geometryCode != null) {
            defines["HAS_GEOMETRY_SHADER"] = " "
        }
        handler = gl { glCreateProgram() }

        if (handler.toLong() == MemoryUtil.NULL) {
            throw ShaderLoadingException()
        }

        val programs = IntArrayList(3)

        programs += load(vertex, ShaderType.VERTEX, null)
        try {
            geometry?.let { programs += load(it, ShaderType.GEOMETRY, geometryCode) }
        } catch (_: FileNotFoundException) {
        }
        programs += load(fragment, ShaderType.FRAGMENT, null)

        for (program in programs) {
            gl { glAttachShader(handler, program) }
        }

        gl { glLinkProgram(handler) }

        gl { glValidateProgram(handler) }

        if (gl { glGetProgrami(handler, GL_LINK_STATUS) } == GL_FALSE) {
            throw ShaderLinkingException("Can not link shaders: $vertex with $geometry with ${fragment}: \n ${glGetProgramInfoLog(handler)}")
        }
        for (program in programs) {
            gl { glDeleteShader(program) }
        }
        loaded = true
    }

    override fun unload() {
        check(loaded) { "Not loaded!" }
        gl { glDeleteProgram(this.handler) }
        loaded = false
        this.handler = -1
    }

    override fun reload() {
        unload()
        load()
    }


    private fun getUniformLocation(uniform: String) = uniformLocations.getOrPut(uniform) {
        val location = gl { glGetUniformLocation(handler, uniform) }
        if (location < 0) {
            val error = "No uniform named $uniform in $this, maybe you use something that has been optimized out? Check your shader code!"
            if (!context.profile.advanced.allowUniformErrors) {
                throw IllegalArgumentException(error)
            }
            Log.log(LogMessageType.RENDERING, LogLevels.WARN, error)
        }
        return@getOrPut location
    }

    override fun setFloat(uniform: String, value: Float) {
        gl { glUniform1f(getUniformLocation(uniform), value) }
    }

    override fun setInt(uniform: String, value: Int) {
        gl { glUniform1i(getUniformLocation(uniform), value) }
    }

    override fun setUInt(uniform: String, value: Int) {
        gl { glUniform1ui(getUniformLocation(uniform), value) }
    }

    override fun setBoolean(uniform: String, boolean: Boolean) {
        setInt(uniform, if (boolean) 1 else 0)
    }

    override fun setMat4f(uniform: String, mat4: Mat4f) {
        gl { glUniformMatrix4fv(getUniformLocation(uniform), false, mat4._0.array) }
    }

    override fun setVec2f(uniform: String, vec2: Vec2f) {
        gl { glUniform2f(getUniformLocation(uniform), vec2.x, vec2.y) }
    }

    override fun setVec3f(uniform: String, vec3: Vec3f) {
        gl { glUniform3f(getUniformLocation(uniform), vec3.x, vec3.y, vec3.z) }
    }

    override fun setVec4f(uniform: String, vec4: Vec4f) {
        gl { glUniform4f(getUniformLocation(uniform), vec4.x, vec4.y, vec4.z, vec4.w) }
    }

    override fun setRGBColor(uniform: String, color: RGBColor) {
        setRGBAColor(uniform, color.rgba())
    }

    override fun setRGBAColor(uniform: String, color: RGBAColor) {
        gl { glUniform4f(getUniformLocation(uniform), color.redf, color.greenf, color.bluef, color.alphaf) }
    }

    override fun setTexture(uniform: String, textureId: Int) {
        gl { glUniform1i(getUniformLocation(uniform), textureId) }
    }

    override fun setUniformBuffer(uniform: String, buffer: UniformBuffer) {
        if (buffer !is OpenGlUniformBuffer) throw IllegalArgumentException("Not an opengl buffer: $buffer")
        val location = uniformLocations.getOrPut(uniform) {
            val index = gl { glGetUniformBlockIndex(handler, uniform) }
            if (index < 0) {
                throw IllegalArgumentException("No uniform buffer called $uniform")
            }
            return@getOrPut index
        }
        gl { glUniformBlockBinding(handler, location, buffer.bindingIndex) }
    }

    fun unsafeUse() {
        gl { glUseProgram(handler) }
    }

    override fun toString(): String {
        return "OpenGLShader: $vertex:$geometry:$fragment"
    }

    private enum class ShaderType(
        val native: Int,
    ) {
        GEOMETRY(GL_GEOMETRY_SHADER),
        VERTEX(GL_VERTEX_SHADER),
        FRAGMENT(GL_FRAGMENT_SHADER),
        COMPUTE(GL_COMPUTE_SHADER),
    }
}
