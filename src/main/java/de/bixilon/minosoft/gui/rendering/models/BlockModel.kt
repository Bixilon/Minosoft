package de.bixilon.minosoft.gui.rendering.models

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.world.InChunkSectionLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class BlockModel(val parent: BlockModel? = null) {
    protected val textures: MutableMap<String, Int> = mutableMapOf()

    open fun render(position: InChunkSectionLocation, direction: Directions): List<Float> {
        val data: MutableList<Float> = mutableListOf()
        val model = Mat4().translate(Vec3(position.x, position.y, position.z))
        val vertexArray = RenderConstants.VERTICIES[direction.ordinal]
        var vertex = 0
        val texture = textures["all"]?.toFloat() ?: -1.0f
        while (vertex < vertexArray.size) {
            val input = Vec4(vertexArray[vertex++], vertexArray[vertex++], vertexArray[vertex++], 1.0f)
            val output = model * input
            // Log.debug("input=%s; position=%s; output=%s;", input, position, output);
            data.add(output.x)
            data.add(output.y)
            data.add(output.z)
            data.add(vertexArray[vertex++])
            data.add(texture) // ToDo: Compact this
        }
        return data
    }


    open fun deserialize(textureIndices: MutableMap<String, Int>, json: JsonObject) {
        json["textures"]?.asJsonObject?.let {
            for ((type, value) in it.entrySet()) {
                if (value.asString.startsWith("#")) {
                    // ToDo
                } else {
                    var index: Int? = textureIndices[value.asString]
                    if (index == null) {
                        index = textureIndices.size
                        textureIndices[value.asString] = index
                    }
                    textures[type] = index
                }
            }
        }
    }

}
