package de.bixilon.minosoft.gui.rendering.chunk.models

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class BlockModelElement(data: JsonObject) {
    private var from: Vec3 = Vec3(0, 0, 0)
    private var to: Vec3 = Vec3(16, 16, 16)
    private val faces: MutableMap<Directions, BlockModelFace> = mutableMapOf()
    val fullFaceDirections: MutableSet<Directions> = mutableSetOf()
    var fullFace = false
    private var positions: Array<Vec3>

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
            for ((directionName, json) in it.asJsonObject.entrySet()) {
                val direction = Directions.valueOf(directionName.toUpperCase())
                when (direction) {
                    Directions.DOWN -> {
                        if ((from.y == 0f || to.y == 0f) && ((from.x == 0f && to.z == 16f) || (from.z == 16f && to.x == 0f))) {
                            fullFace = true
                        }
                    }
                    Directions.UP -> {
                        if ((from.y == 16f || to.y == 16f) && ((from.x == 0f && to.z == 16f) || (from.z == 16f && to.x == 0f))) {
                            fullFace = true
                        }
                    }
                    Directions.NORTH -> {
                        if ((from.x == 0f || to.x == 0f) && ((from.y == 0f && to.y == 16f) || (from.z == 16f && to.z == 0f))) {
                            fullFace = true
                        }
                    }
                    Directions.SOUTH -> {
                        if ((from.x == 16f || to.x == 16f) && ((from.y == 0f && to.y == 16f) || (from.z == 16f && to.z == 0f))) {
                            fullFace = true
                        }
                    }
                    Directions.EAST -> {
                        if ((from.z == 0f || to.z == 0f) && ((from.y == 0f && to.y == 16f) || (from.x == 16f && to.x == 0f))) {
                            fullFace = true
                        }
                    }
                    Directions.WEST -> {
                        if ((from.z == 16f || to.z == 16f) && ((from.y == 0f && to.y == 16f) || (from.x == 16f && to.x == 0f))) {
                            fullFace = true
                        }
                    }
                }
                faces[direction] = BlockModelFace(json.asJsonObject)
                if (fullFace) {
                    fullFaceDirections.add(direction)
                }
            }
        }
        positions = arrayOf(
            Vec3(from),
            Vec3(to.x,      from.y,     from.z),
            Vec3(from.x,    from.y,     to.z),
            Vec3(to.x,      from.y,     to.z),
            Vec3(from.x,    to.y,       from.z),
            Vec3(to.x,      to.y,       from.z),
            Vec3(from.x,    to.y,       to.z),
            Vec3(to),
        )

        fun getRotatedValues(x: Float, y: Float, sin: Double, cos: Double): Pair<Float, Float> {
            return Pair((x * cos - y * sin).toFloat(), (x * sin + y * cos).toFloat())
        }

        fun rotate(axis: Axes, angle: Double, origin: Vec3) {
            // TODO: optimize for 90deg, 180deg, 270deg rotations
            val sin = glm.sin(glm.radians(angle))
            val cos = glm.cos(glm.radians(angle))
            for ((i, position) in positions.withIndex()) {
                val transformedPosition = position - origin
                when (axis) {
                    Axes.X -> {
                        val rotatedValues = getRotatedValues(transformedPosition.y, transformedPosition.z, sin, cos)
                        transformedPosition.y = rotatedValues.first
                        transformedPosition.z = rotatedValues.second
                    }
                    Axes.Y -> {
                        val rotatedValues = getRotatedValues(transformedPosition.x, transformedPosition.z, sin, cos)
                        transformedPosition.x = rotatedValues.first
                        transformedPosition.z = rotatedValues.second
                    }
                    Axes.Z -> {
                        val rotatedValues = getRotatedValues(transformedPosition.x, transformedPosition.y, sin, cos)
                        transformedPosition.x = rotatedValues.first
                        transformedPosition.y = rotatedValues.second
                    }
                }
                positions[i] = transformedPosition + origin
            }
        }
        data["rotation"]?.let {
            val rotation = it.asJsonObject
            rotate(Axes.valueOf(rotation["axis"].asString.toUpperCase()), rotation["angle"].asDouble, jsonArrayToVec3(rotation["origin"].asJsonArray))
        }
        for ((i, position) in positions.withIndex()) {
            positions[i] = BlockModel.transformPosition(position)
        }
    }

    open fun render(textureMapping: MutableMap<String, Texture>, modelMatrix: Mat4, direction: Directions, rotation: Vec3, data: MutableList<Float>) {
        val face = faces[direction] ?: return // Not our face

        val texture = textureMapping[face.textureName] ?: TextureArray.DEBUG_TEXTURE
        if (texture.isTransparent) {
            return
        }

        fun addToData(vec3: Vec3, textureCoordinates: Vec2) {
            val input = Vec4(vec3, 1.0f)
            val output = modelMatrix * input
            data.add(output.x)
            data.add(output.y)
            data.add(output.z)
            data.add(textureCoordinates.x)
            data.add(textureCoordinates.y)
            data.add(texture.id.toFloat()) // ToDo: Compact this
        }

        fun createQuad(drawPositions: Array<Vec3>, texturePosition1: Vec2, texturePosition2: Vec2, texturePosition3: Vec2, texturePosition4: Vec2) {
            addToData(drawPositions[0], texturePosition2)
            addToData(drawPositions[3], texturePosition3)
            addToData(drawPositions[2], texturePosition4)
            addToData(drawPositions[2], texturePosition4)
            addToData(drawPositions[1], texturePosition1)
            addToData(drawPositions[0], texturePosition2)
        }

        val positionTemplate = FACE_POSITION_MAP_TEMPLATE[direction.ordinal]
        val drawPositions = arrayOf(positions[positionTemplate[0]], positions[positionTemplate[1]], positions[positionTemplate[2]], positions[positionTemplate[3]])
        when (direction) {
            Directions.DOWN ->  createQuad(drawPositions, face.texturLeftDown, face.texturLeftUp, face.texturRightUp, face.texturRightDown)
            Directions.UP ->    createQuad(drawPositions, face.texturLeftDown, face.texturLeftUp, face.texturRightUp, face.texturRightDown)
            Directions.NORTH -> createQuad(drawPositions, face.texturRightDown, face.texturRightUp, face.texturLeftUp, face.texturLeftDown)
            Directions.SOUTH -> createQuad(drawPositions, face.texturLeftDown, face.texturLeftUp, face.texturRightUp, face.texturRightDown)
            Directions.WEST ->  createQuad(drawPositions, face.texturRightUp, face.texturRightDown, face.texturLeftDown, face.texturLeftUp)
            Directions.EAST ->  createQuad(drawPositions, face.texturLeftUp, face.texturLeftDown, face.texturRightDown, face.texturRightUp)
        }
    }

    fun isCullFace(direction: Directions): Boolean {
        return faces[direction]?.cullFace == direction
    }

    fun getTexture(direction: Directions): String? {
        return faces[direction]?.textureName
    }

    companion object {
        fun jsonArrayToVec3(array: JsonArray) : Vec3 {
            return Vec3(array[0].asFloat, array[1].asFloat, array[2].asFloat)
        }

        val FACE_POSITION_MAP_TEMPLATE = arrayOf(intArrayOf(2, 3, 1, 0), intArrayOf(4, 5, 7, 6), intArrayOf(1, 5, 4, 0), intArrayOf(2, 6, 7, 3), intArrayOf(6, 2, 0, 4), intArrayOf(5, 1, 3, 7))
    }
}
