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

import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader.Companion.shader
import de.bixilon.minosoft.gui.rendering.system.base.shader.ShaderManagement
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import org.lwjgl.opengl.GL30.glUseProgram

class OpenGlShaderManagement(val system: OpenGlRenderSystem) : ShaderManagement {
    private val shaders: MutableSet<Shader> = mutableSetOf()

    override var shader: Shader? = null
        set(value) {
            if (value?.native === field?.native) return

            if (value == null) {
                gl { glUseProgram(0) }
                field = null
                return
            }
            val native = value.native

            check(native is OpenGlNativeShader) { "Can not use non OpenGL shader in OpenGL render system!" }
            check(native.loaded) { "Shader not loaded!" }
            check(system === native.system) { "Shader not part of this context!" }

            native.unsafeUse()

            field = value
        }


    override fun create(vertex: ResourceLocation, geometry: ResourceLocation?, fragment: ResourceLocation): OpenGlNativeShader {
        return OpenGlNativeShader(system, vertex.shader(), geometry?.shader(), fragment.shader())
    }

    override fun plusAssign(shader: Shader) {
        this.shaders += shader
    }

    override fun minusAssign(shader: Shader) {
        this.shaders -= shader
    }

    override fun iterator() = shaders.iterator()
}
