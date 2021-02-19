package de.bixilon.minosoft.gui.rendering.chunk.models.loading

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class BlockModelFace(data: JsonObject, from: Vec3, to: Vec3, direction: Directions) {
    val textureName: String = data.get("texture").asString.removePrefix("#")
    val cullFace: Directions?

    private var positions: Array<Vec2>

    init {
        var textureStart = Vec2(0, 0)
        var textureEnd = Vec2(16, 16)
        when (direction) {
            Directions.EAST, Directions.WEST -> run {
                textureStart = Vec2(from.z.toInt(),  16 - from.y.toInt());
                textureEnd = Vec2(to.z.toInt(), 16 - to.y.toInt());
            }
            Directions.UP, Directions.DOWN -> {
                textureStart = Vec2(from.x.toInt(), 16 - from.z.toInt());
                textureEnd = Vec2(to.x.toInt(), 16 - to.z.toInt());
            }
            Directions.NORTH, Directions.SOUTH -> {
                textureStart = Vec2(from.x.toInt(), 16 - from.y.toInt());
                textureEnd = Vec2(to.x.toInt(), 16 - to.y.toInt());
            }
        }
        data["uv"]?.asJsonArray?.let {
            textureStart = Vec2(it[0].asFloat, it[1].asFloat)
            textureEnd = Vec2(it[2].asFloat, it[3].asFloat)
        }
        positions = arrayOf(
            uvToFloat(Vec2(textureStart.x, textureStart.y)),
            uvToFloat(Vec2(textureStart.x, textureEnd.y)),
            uvToFloat(Vec2(textureEnd.x, textureEnd.y)),
            uvToFloat(Vec2(textureEnd.x, textureStart.y)),
                           )

        cullFace = data["cullface"]?.asString?.let {
            return@let if (it == "bottom") {
                Directions.DOWN
            } else {
                Directions.valueOf(it.toUpperCase())
            }
        }
        positions = arrayOf(
            Vec2(uvToFloat(textureStart.x),  uvToFloat(textureStart.y)),
            Vec2(uvToFloat(textureStart.x),  uvToFloat(textureEnd.y)),
            Vec2(uvToFloat(textureEnd.x),    uvToFloat(textureEnd.y)),
            Vec2(uvToFloat(textureEnd.x),    uvToFloat(textureStart.y)),
                           )
    }

    fun getTexturePositionArray(direction: Directions): Array<Vec2?> {
        val template = textureTemplate[direction.ordinal]
        val result = arrayOfNulls<Vec2>(template.size)
        for (i in template.indices) {
            result[i] = positions[template[i]]
        }
        return result
    }


    companion object {
        private fun uvToFloat(uv: Float): Float {
            return (uv) / 16f
        }

        fun uvToFloat(vec2: Vec2): Vec2 {
            return Vec2(uvToFloat(vec2.x), uvToFloat(vec2.y))
        }

        val textureTemplate = arrayOf(
            arrayOf(0, 1, 2, 3, ),
            arrayOf(0, 1, 2, 3, ),
            arrayOf(3, 2, 1, 0, ),
            arrayOf(0, 1, 2, 3, ),
            arrayOf(2, 3, 0, 1, ),
            arrayOf(1, 0, 3, 2, ),
                                     )
    }
}
