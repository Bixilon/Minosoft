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

package de.bixilon.minosoft.gui.rendering.system.base.shader.code.glsl

import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.minosoft.commands.util.StringReader
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext

class GLSLShaderCode(
    private val context: RenderContext,
    private val rawCode: String,
    private val file: ResourceLocation? = null,
) {
    val defines: MutableMap<String, Any> = mutableMapOf()

    init {
        defines[context.system.vendor.define] = ""
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
                        val rawInclude = reader.readString()!!

                        val include = when {
                            rawInclude.startsWith("../") -> {
                                if (file == null) throw IllegalStateException("Can not include from relative paths: file is null!")

                                var normalized = rawInclude
                                var up = 0
                                while (normalized.startsWith("../")) {
                                    normalized = normalized.removePrefix("../")
                                    up += 1
                                }

                                var split = file.path.split("/")
                                if (split.size <= up) throw IllegalArgumentException("Can not traverse relative path: $rawInclude (parent does not exist)")

                                split = split.subList(0, split.size - up)

                                ResourceLocation(file.namespace, split.joinToString("/") + "/" + normalized.appendEnding())
                            }

                            rawInclude.startsWith("./") -> {
                                if (file == null) throw IllegalStateException("Can not include from relative paths: file is null!")

                                ResourceLocation(file.namespace, file.path.split("/").let { it.subList(0, it.size - 1) }.joinToString("/") + "/" + rawInclude.removePrefix("./").appendEnding())
                            }

                            else -> ResourceLocation.of(rawInclude).let { ResourceLocation(it.namespace, "rendering/shader/includes/${it.path.appendEnding()}") }

                        }

                        val includeCode = GLSLShaderCode(context, context.session.assetsManager[include].readAsString())

                        code.appendLine()
                        code.append("// ").append(STAR).appendLine()
                        code.append("// Begin included from ").append(rawInclude).appendLine()
                        code.append("// ").append(STAR).appendLine()
                        code.append(includeCode.code)
                        code.appendLine()
                        code.append("// ").append(STAR).appendLine()
                        code.append("// End include from ").append(include).appendLine()
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

        private fun String.appendEnding(): String {
            if (this.endsWith(".vsh")) return this
            if (this.endsWith(".gsh")) return this
            if (this.endsWith(".fsh")) return this
            if (this.endsWith(".glsl")) return this

            return "$this.glsl"
        }
    }
}
