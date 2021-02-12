package de.bixilon.minosoft.gui.rendering.chunk.models

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import glm_.vec2.Vec2

class BlockModelFace(data: JsonObject) {
    val textureName: String = data.get("texture").asString.removePrefix("#")
    val cullFace: Directions?

    var textureStart = Vec2(0, 0)
    var textureEnd = Vec2(16, 16)

    init {
        data["uv"]?.asJsonArray?.let {
            textureStart = Vec2(it[0].asFloat, it[1].asFloat)
            textureEnd = Vec2(it[2].asFloat, it[3].asFloat)
        }
        cullFace = data["cullface"]?.asString?.let {
            return@let if (it == "bottom") {
                Directions.DOWN
            } else {
                Directions.valueOf(it.toUpperCase())
            }
        }
    }

    val textureLeftDown = Vec2(uvToFloat(textureStart.x), uvToFloat(textureStart.y))
    val textureLeftUp = Vec2(uvToFloat(textureStart.x), uvToFloat(textureEnd.y))
    val textureRightUp = Vec2(uvToFloat(textureEnd.x), uvToFloat(textureEnd.y))
    val textureRightDown = Vec2(uvToFloat(textureEnd.x), uvToFloat(textureStart.y))


    companion object {
        fun uvToFloat(uv: Float): Float {
            return (uv) / 16f
        }
    }
}
