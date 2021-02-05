package de.bixilon.minosoft.gui.rendering.models

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.world.InChunkSectionLocation
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class BlockModelElement(data: JsonObject) {
    private var from: Vec3 = Vec3(0, 0, 0)
    private var to: Vec3 = Vec3(16, 16, 16)
    private val faces: MutableMap<Directions, BlockModelFace> = mutableMapOf()

    init {
        data["from"]?.let {
            val array = it.asJsonArray
            from = Vec3(array[0].asFloat, array[1].asFloat, array[2].asFloat)
        }
        data["to"]?.let {
            val array = it.asJsonArray
            to = Vec3(array[0].asFloat, array[1].asFloat, array[2].asFloat)
        }
        data["faces"]?.let {
            for ((direction, json) in it.asJsonObject.entrySet()) {
                faces[Directions.valueOf(direction.toUpperCase())] = BlockModelFace(json.asJsonObject)
            }
        }
    }

    private val positionUpLeftFront = Vec3(BlockModel.positionToFloat(from.x), BlockModel.positionToFloat(to.y), BlockModel.positionToFloat(from.z))
    private val positionUpLeftBack = Vec3(BlockModel.positionToFloat(from.x), BlockModel.positionToFloat(to.y), BlockModel.positionToFloat(to.z))
    private val positionUpRightFront = Vec3(BlockModel.positionToFloat(to.x), BlockModel.positionToFloat(to.y), BlockModel.positionToFloat(from.z))
    private val positionUpRightBack = Vec3(BlockModel.positionToFloat(to.x), BlockModel.positionToFloat(to.y), BlockModel.positionToFloat(to.z))

    private val positionDownLeftFront = Vec3(BlockModel.positionToFloat(from.x), BlockModel.positionToFloat(from.y), BlockModel.positionToFloat(from.z))
    private val positionDownLeftBack = Vec3(BlockModel.positionToFloat(from.x), BlockModel.positionToFloat(from.y), BlockModel.positionToFloat(to.z))
    private val positionDownRightFront = Vec3(BlockModel.positionToFloat(to.x), BlockModel.positionToFloat(from.y), BlockModel.positionToFloat(from.z))
    private val positionDownRightBack = Vec3(BlockModel.positionToFloat(to.x), BlockModel.positionToFloat(from.y), BlockModel.positionToFloat(to.z))

    open fun render(textureIndexMap: Map<String, Int>, position: InChunkSectionLocation, direction: Directions): List<Float> {
        val face = faces[direction] ?: return emptyList()
        val data: MutableList<Float> = mutableListOf()
        val model = Mat4().translate(Vec3(position.x, position.y, position.z))
        val texture = textureIndexMap[face.texture.removePrefix("#")]?.toFloat() ?: 0f

        fun addToData(vec3: Vec3, textureCoordinates: Vec2) {
            val input = Vec4(vec3, 1.0f)
            val output = model * input
            data.add(output.x)
            data.add(output.y)
            data.add(output.z)
            data.add(textureCoordinates.x)
            data.add(textureCoordinates.y)
            data.add(texture) // ToDo: Compact this
        }

        fun createQuad(first: Vec3, second: Vec3, third: Vec3, fourth: Vec3) {
            addToData(first, face.texturRightDown)
            addToData(fourth, face.texturRightUp)
            addToData(third, face.texturLeftUp)
            addToData(third, face.texturLeftUp)
            addToData(second, face.texturLeftDown)
            addToData(first, face.texturRightDown)
        }

        when (direction) {
            Directions.DOWN -> createQuad(positionDownLeftFront, positionDownLeftBack, positionDownRightBack, positionDownRightFront)
            Directions.UP -> createQuad(positionUpLeftFront, positionUpLeftBack, positionUpRightBack, positionUpRightFront)
            Directions.NORTH -> createQuad(positionDownLeftFront, positionUpLeftFront, positionUpRightFront, positionDownRightFront)
            Directions.SOUTH -> createQuad(positionDownLeftBack, positionUpLeftBack, positionUpRightBack, positionDownRightBack)
            Directions.WEST -> createQuad(positionUpLeftBack, positionDownLeftBack, positionDownLeftFront, positionUpLeftFront)
            Directions.EAST -> createQuad(positionUpRightBack, positionDownRightBack, positionDownRightFront, positionUpRightFront)
        }

        return data
    }
}
