package de.bixilon.minosoft.gui.rendering.models

import com.google.gson.JsonObject
import glm_.vec2.Vec2

class BlockModelFace(data: JsonObject) {
    val texture: String = data.get("texture").asString
    var textureStart = Vec2(0, 0)
    var textureEnd = Vec2(16, 16)

    init {
        data["uv"]?.asJsonArray?.let {
            textureStart = Vec2(it[0].asFloat, it[1].asFloat)
            textureEnd = Vec2(it[2].asFloat, it[3].asFloat)
        }
    }

    val texturLeftDown = Vec2(uvToFloat(textureStart.x), uvToFloat(textureStart.y))
    val texturLeftUp = Vec2(uvToFloat(textureStart.x), uvToFloat(textureEnd.y))
    val texturRightUp = Vec2(uvToFloat(textureEnd.x), uvToFloat(textureEnd.y))
    val texturRightDown = Vec2(uvToFloat(textureEnd.x), uvToFloat(textureStart.y))


    companion object {
        fun uvToFloat(uv: Float): Float {
            return (uv) / 16f
        }
    }
}
