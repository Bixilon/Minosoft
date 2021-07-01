/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.base.shader.code.glsl

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader

class GLSLShaderCode(
    private val renderWindow: RenderWindow,
    private val rawCode: String,
) {
    val defines: MutableMap<String, Any> = mutableMapOf()
    val uniforms: MutableSet<String> = mutableSetOf()

    init {
        // ToDo: This is complete trash and should be replaced

        for (line in rawCode.lines()) {
            if (!line.startsWith("uniform ")) {
                continue
            }
            val reader = GLSLStringReader(line.removePrefix("uniform "))
            reader.skipWhitespaces()
            reader.readUnquotedString() // data type
            reader.skipWhitespaces()

            uniforms += reader.readUnquotedString()
        }

        for ((name, value) in Shader.DEFAULT_DEFINES) {
            value(renderWindow)?.let { defines[name] = it }
        }
        defines[renderWindow.renderSystem.vendor.shaderDefine] = ""
    }

    val code: String
        get() {
            // ToDo: This is complete trash and should be replaced
            val code = StringBuilder()

            for (line in rawCode.lines()) {

                fun pushLine() {
                    code.append(line)
                    code.append('\n')
                }
                when {
                    line.startsWith("#include ") -> {
                        val reader = GLSLStringReader(line.removePrefix("#include "))
                        reader.skipWhitespaces()

                        val include = ResourceLocation(reader.readString())

                        val includeCode = GLSLShaderCode(renderWindow, renderWindow.connection.assetsManager.readStringAsset(ResourceLocation(include.namespace, "rendering/shader/includes/${include.path}.glsl")))
                        this.uniforms += includeCode.uniforms

                        code.append('\n')
                        code.append(includeCode.code)
                        code.append('\n')
                    }
                    line.startsWith("#version") -> {
                        pushLine()

                        for ((name, value) in defines) {
                            code.append("#define ")
                            code.append(name)
                            code.append(' ')
                            code.append(value)
                            code.append('\n')
                        }
                    }
                    else -> pushLine()
                }
            }

            return code.toString()
        }
}
