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
    private val faces: MutableMap<Directions, BlockModelFace> = mutableMapOf()
    val fullFaceDirections: MutableSet<Directions> = mutableSetOf()
    var fullFace = false
    private var positions: Array<Vec3>

    init {
        var from = Vec3(0, 0, 0)
        var to = Vec3(16, 16, 16)
        data["from"]?.let {
            val array = it.asJsonArray
            from = Vec3(array[0].asFloat, array[1].asFloat, array[2].asFloat)
        }
        data["to"]?.let {
            val array = it.asJsonArray
            to = Vec3(array[0].asFloat, array[1].asFloat, array[2].asFloat)
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
        var rotate = Vec3()
        data["rotation"]?.let {
            val rotation = it.asJsonObject
            val axis = Axes.valueOf(rotation["axis"].asString.toUpperCase())
            val angle = glm.radians(rotation["angle"].asDouble)
            rotate(axis, angle, jsonArrayToVec3(rotation["origin"].asJsonArray))
            rotate = when (axis) {
                Axes.X -> Vec3(angle, 0, 0)
                Axes.Y -> Vec3(0, angle, 0)
                Axes.Z -> Vec3(0, 0, angle)
            }
        }
        data["faces"]?.let {
            for ((directionName, json) in it.asJsonObject.entrySet()) {
                var direction = Directions.valueOf(directionName.toUpperCase())
                faces[direction] = BlockModelFace(json.asJsonObject)
                direction = getRotatedDirection(rotate, direction)
                fullFace = positions.containsAll(fullTestPositions[direction]) // TODO: check if texture is transparent ==> && ! texture.isTransparent
                if (fullFace) {
                    fullFaceDirections.add(direction)
                }
            }
        }
        for ((i, position) in positions.withIndex()) {
            positions[i] = BlockModel.transformPosition(position)
        }
    }

    fun rotate(axis: Axes, angle: Double, origin: Vec3) {
        // TODO: optimize for 90deg, 180deg, 270deg rotations
        for ((i, position) in positions.withIndex()) {
            var transformedPosition = position - origin
            transformedPosition = rotateVector(transformedPosition, angle, axis)
            positions[i] = transformedPosition + origin
        }
    }

    private fun rotateVector(original: Vec3, angle: Double, axis: Axes): Vec3 {
        fun getRotatedValues(x: Float, y: Float, sin: Double, cos: Double): Pair<Float, Float> {
            return Pair((x * cos - y * sin).toFloat(), (x * sin + y * cos).toFloat())
        }
        return when (axis) {
            Axes.X -> run {
                val rotatedValues = getRotatedValues(original.y, original.z, glm.sin(angle), glm.cos(angle))
                return@run Vec3(original.x, rotatedValues.first, rotatedValues.second)
            }
            Axes.Y -> run {
                val rotatedValues = getRotatedValues(original.x, original.z, glm.sin(angle), glm.cos(angle))
                return@run Vec3(rotatedValues.first, original.y, rotatedValues.second)
            }
            Axes.Z -> run {
                val rotatedValues = getRotatedValues(original.x, original.y, glm.sin(angle), glm.cos(angle))
                return@run Vec3(rotatedValues.first, rotatedValues.second, original.z)
            }
        }
    }

    private fun getRotatedDirection(rotation: Vec3, direction: Directions): Directions {
        if (rotation == Vec3(0, 0, 0)) {
            return direction
        }
        var rotatedDirectionVector = rotateVector(direction.directionVector, rotation.z.toDouble(), Axes.Z)
        rotatedDirectionVector = rotateVector(rotatedDirectionVector, rotation.y.toDouble(), Axes.Y)
        return Directions.byDirection(rotateVector(rotatedDirectionVector, rotation.x.toDouble(), Axes.X))
    }

    open fun render(textureMapping: MutableMap<String, Texture>, modelMatrix: Mat4, direction: Directions, rotation: Vec3, data: MutableList<Float>) {
        val realDirection = getRotatedDirection(rotation, direction)
        val positionTemplate = FACE_POSITION_MAP_TEMPLATE[realDirection.ordinal]

        val face = faces[realDirection] ?: return // Not our face
        val texture = textureMapping[face.textureName] ?: TextureArray.DEBUG_TEXTURE
        // if (texture.isTransparent) {
        //     return // ToDo: force render transparent faces
        // }

        val drawPositions = arrayOf(positions[positionTemplate[0]], positions[positionTemplate[1]], positions[positionTemplate[2]], positions[positionTemplate[3]])

        fun addToData(vec3: Vec3, textureCoordinates: Vec2) {
            val input = Vec4(vec3, 1.0f)
            val output = modelMatrix * input
            data.add(output.x)
            data.add(output.y)
            data.add(output.z)
            data.add(textureCoordinates.x * texture.widthFactor)
            data.add(textureCoordinates.y * texture.heightFactor)
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

        when (realDirection) {
            Directions.DOWN ->  createQuad(drawPositions, face.textureLeftDown, face.textureLeftUp, face.textureRightUp, face.textureRightDown)
            Directions.UP ->    createQuad(drawPositions, face.textureLeftDown, face.textureLeftUp, face.textureRightUp, face.textureRightDown)
            Directions.NORTH -> createQuad(drawPositions, face.textureRightDown, face.textureRightUp, face.textureLeftUp, face.textureLeftDown)
            Directions.SOUTH -> createQuad(drawPositions, face.textureLeftDown, face.textureLeftUp, face.textureRightUp, face.textureRightDown)
            Directions.WEST ->  createQuad(drawPositions, face.textureRightUp, face.textureRightDown, face.textureLeftDown, face.textureLeftUp)
            Directions.EAST ->  createQuad(drawPositions, face.textureLeftUp, face.textureLeftDown, face.textureRightDown, face.textureRightUp)
        }
    }

    fun isCullFace(direction: Directions): Boolean {
        return faces[direction]?.cullFace == direction
    }

    fun getTexture(direction: Directions): String? {
        return faces[direction]?.textureName
    }

    companion object {
        fun jsonArrayToVec3(array: JsonArray): Vec3 {
            return Vec3(array[0].asFloat, array[1].asFloat, array[2].asFloat)
        }

        private const val BLOCK_RESOLUTION = 16

        val FACE_POSITION_MAP_TEMPLATE = arrayOf(intArrayOf(0, 2, 3, 1), intArrayOf(6, 4, 5, 7), intArrayOf(1, 5, 4, 0), intArrayOf(2, 6, 7, 3), intArrayOf(6, 2, 0, 4), intArrayOf(5, 1, 3, 7))

        private val POSITION_1 = Vec3(0, 0, 0)
        private val POSITION_2 = Vec3(BLOCK_RESOLUTION, 0, 0)
        private val POSITION_3 = Vec3(0, 0, BLOCK_RESOLUTION)
        private val POSITION_4 = Vec3(BLOCK_RESOLUTION, 0, BLOCK_RESOLUTION)

        private val POSITION_5 = Vec3(0, BLOCK_RESOLUTION, 0)
        private val POSITION_6 = Vec3(BLOCK_RESOLUTION, BLOCK_RESOLUTION, 0)
        private val POSITION_7 = Vec3(0, BLOCK_RESOLUTION, BLOCK_RESOLUTION)
        private val POSITION_8 = Vec3(BLOCK_RESOLUTION, BLOCK_RESOLUTION, BLOCK_RESOLUTION)

        val fullTestPositions = mapOf(
            Pair(Directions.EAST, setOf(POSITION_1, POSITION_3, POSITION_5, POSITION_7)),
            Pair(Directions.WEST, setOf(POSITION_2, POSITION_4, POSITION_6, POSITION_8)),
            Pair(Directions.DOWN, setOf(POSITION_1, POSITION_2, POSITION_3, POSITION_4)),
            Pair(Directions.UP, setOf(POSITION_5, POSITION_6, POSITION_7, POSITION_8)),
            Pair(Directions.SOUTH, setOf(POSITION_1, POSITION_2, POSITION_5, POSITION_6)),
            Pair(Directions.NORTH, setOf(POSITION_3, POSITION_4, POSITION_7, POSITION_8)),
        )
    }
}

private fun <T> Array<T>.containsAll(set: Set<T>?): Boolean {
    set?.let {
        for (value in set) {
            if (!this.contains(value)) {
                return false
            }
        }
        return true
    }
    return false
}
