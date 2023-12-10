/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.minosoft.assets.util.InputStreamUtil.readAsString
import de.bixilon.minosoft.commands.util.StringReader
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader

class GLSLShaderCode(
    private val context: RenderContext,
    private val rawCode: String,
) {
    val defines: MutableMap<String, Any> = mutableMapOf()

    init {
        for ((name, value) in NativeShader.DEFAULT_DEFINES) {
            value(context)?.let { defines[name] = it }
        }
        defines[context.system.vendor.shaderDefine] = ""
    }

    val code: String
        get() {
            // ToDo: This is complete trash and should be replaced
            val code = StringBuilder()

            for (line in rawCode.lines()) {
                val lineReader = StringReader(line)
                lineReader.skipWhitespaces()

                val remaining = lineReader.peekRemaining() ?: continue
                fun pushLine() {
                    code.append(remaining)
                    code.appendLine()
                }

                when {
                    remaining.startsWith("#include ") -> {
                        // TODO: Don't include multiple times, cache include
                        val reader = GLSLStringReader(remaining.removePrefix("#include "))
                        reader.skipWhitespaces()

                        val include = ResourceLocation.of(reader.readString()!!)

                        val includeCode = GLSLShaderCode(context, context.connection.assetsManager[ResourceLocation(include.namespace, "rendering/shader/includes/${include.path}.glsl")].readAsString())

                        code.appendLine()
                        code.append("// ").append(STAR).appendLine()
                        code.append("// Begin included from $include:\n")
                        code.append("// ").append(STAR).appendLine()
                        code.append(includeCode.code)
                        code.appendLine()
                        code.append("// ").append(STAR).appendLine()
                        code.append("// End include from $include:\n")
                        code.append("// ").append(STAR).appendLine()
                    }

                    remaining.startsWith("#version") -> {
                        pushLine()

                        for ((name, value) in defines) {
                            code.append("#define ")
                            code.append(name)
                            code.append(' ')
                            code.append(value)
                            code.appendLine()
                        }
                    }

                    remaining.startsWith("//") -> continue
                    remaining.startsWith("/*") -> continue
                    remaining.startsWith("*/") -> continue
                    remaining.startsWith("*") -> continue
                    else -> pushLine()
                }
            }

            return code.toString()
        }

    private companion object {
        val STAR = "*".repeat(100)
    }
}
