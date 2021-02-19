package de.bixilon.minosoft.gui.rendering.chunk.models.loading

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import glm_.glm
import glm_.vec3.Vec3

open class BlockModelElement(data: JsonObject) {
    val faces: MutableMap<Directions, BlockModelFace> = mutableMapOf()
    val fullFaceDirections: MutableSet<Directions> = mutableSetOf()
    var fullFace = false
    var positions: Array<Vec3>

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
            rotatePositions(positions, axis, angle, jsonArrayToVec3(rotation["origin"].asJsonArray))
            rotate = when (axis) {
                Axes.X -> run { return@run Vec3(angle, 0, 0) }
                Axes.Y -> run { return@run Vec3(0, angle, 0) }
                Axes.Z -> run { return@run Vec3(0, 0, angle) }
            }
        }
        data["faces"]?.let {
            for ((directionName, json) in it.asJsonObject.entrySet()) {
                var direction = Directions.valueOf(directionName.toUpperCase())
                faces[direction] = BlockModelFace(json.asJsonObject, from, to, direction)
                direction = getRotatedDirection(rotate, direction)
                fullFace = positions.containsAll(fullTestPositions[direction]) // TODO: check if texture is transparent ==> && ! texture.isTransparent
                if (fullFace) {
                    fullFaceDirections.add(direction)
                }
            }
        }
        for ((i, position) in positions.withIndex()) {
            positions[i] = transformPosition(position)
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
        private const val BLOCK_RESOLUTION = 16

        val FACE_POSITION_MAP_TEMPLATE = arrayOf(
            intArrayOf(0, 2, 3, 1),
            intArrayOf(6, 4, 5, 7),
            intArrayOf(1, 5, 4, 0),
            intArrayOf(2, 6, 7, 3),
            intArrayOf(6, 2, 0, 4),
            intArrayOf(5, 1, 3, 7)
                                                )

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

        fun getRotatedDirection(rotation: Vec3, direction: Directions): Directions {
            if (rotation == Vec3(0, 0, 0)) {
                return direction
            }
            var rotatedDirectionVector = rotateVector(direction.directionVector, rotation.z.toDouble(), Axes.Z)
            rotatedDirectionVector = rotateVector(rotatedDirectionVector, rotation.y.toDouble(), Axes.Y)
            return Directions.byDirection(rotateVector(rotatedDirectionVector, rotation.x.toDouble(), Axes.X))
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

        fun rotatePositions(positions: Array<Vec3>, axis: Axes, angle: Double, origin: Vec3) {
            // TODO: optimize for 90deg, 180deg, 270deg rotations
            if (angle == 0.0) {
                return
            }
            for ((i, position) in positions.withIndex()) {
                var transformedPosition = position - origin
                transformedPosition = rotateVector(transformedPosition, angle, axis)
                positions[i] = transformedPosition + origin
            }
        }

        fun rotatePositionsAxes(positions: Array<Vec3>, angles: Vec3) {
            rotatePositions(positions, Axes.Z, angles.z.toDouble(), Vec3())
            rotatePositions(positions, Axes.Y, angles.y.toDouble(), Vec3())
            rotatePositions(positions, Axes.X, angles.x.toDouble(), Vec3())
        }

        fun transformPosition(position: Vec3): Vec3 {
            fun positionToFloat(uv: Float): Float {
                return (uv - 8f) / 16f
            }
            return Vec3(positionToFloat(position.x), positionToFloat(position.y), positionToFloat(position.z))
        }
    }
}

private fun <T> Array<T>.containsAll(set: Set<T>?): Boolean {
    if (set != null) {
        for (value in set) {
            if (! this.contains(value)) {
                return false;
            }
        }
        return true
    }
    return false
}
