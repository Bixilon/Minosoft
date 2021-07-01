package de.bixilon.minosoft.gui.rendering.system.base.shader

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.commands.CommandStringReader
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow

object GLSLUtil {

    fun readGLSL(assetsManager: AssetsManager = Minosoft.MINOSOFT_ASSETS_MANAGER, renderWindow: RenderWindow, resourceLocation: ResourceLocation, defines: Map<String, Any>, uniforms: MutableList<String>): String {
        val total = StringBuilder()
        val lines = assetsManager.readStringAsset(resourceLocation).lines()

        for (line in lines) {
            val reader = CommandStringReader(line)
            when {
                line.startsWith("#include ") -> {
                    val includeResourceLocation = ResourceLocation(
                        line.removePrefix("#include ").removePrefix("\"").removeSuffix("\"").replace("\\\"", "\"")
                    )
                    total.append("\n")
                    total.append(
                        assetsManager.readStringAsset(
                            if (includeResourceLocation.path.contains(".glsl")) {
                                includeResourceLocation
                            } else {
                                ResourceLocation(
                                    includeResourceLocation.namespace,
                                    "rendering/shader/includes/${includeResourceLocation.path}.glsl"
                                )
                            }
                        )
                    )

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

                    for ((name, value) in Shader.DEFAULT_DEFINES) {
                        value(renderWindow)?.let { pushDefine(name, it) }
                    }

                    pushDefine(renderWindow.renderSystem.vendor.shaderDefine, "")
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
        return total.toString()
    }
}
