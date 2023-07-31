/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.models.block.state.apply

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.DirectionUtil.rotateX
import de.bixilon.minosoft.data.direction.DirectionUtil.rotateY
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.face.FaceUV
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.compact
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.compactProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.positions
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.pushRight
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

data class SingleBlockStateApply(
    val model: BlockModel,
    val uvLock: Boolean = false,
    val x: Int = 0,
    val y: Int = 0,
) : BlockStateApply {

    private fun FloatArray.rotateX(count: Int) {

        fun FloatArray.rotateOffsetX(offset: Int) {
            val y = this[offset + 1]
            val z = this[offset + 2]

            this[offset + 1] = z
            this[offset + 2] = -y + 1.0f
        }

        for (c in 0 until count) {
            for (i in 0 until 4) {
                rotateOffsetX(i * 3)
            }
        }
    }

    private fun FloatArray.rotateX(direction: Directions): FloatArray {
        if (x == 0) return this

        rotateX(x)
        if (direction.axis == Axes.X) {
            return pushRight(3, if (direction.negative) -x else x)
        }

        if (direction.axis == Axes.Y && (x == 2 || x == 3)) {
            return pushRight(3, if (direction.negative) -1 else 1)
        } else if (direction.axis == Axes.Z && (x == 1 || x == 2)) {
            return pushRight(3, if (direction.negative) 1 else -1)
        }

        return this
    }


    private fun FloatArray.rotateY(count: Int) {

        fun FloatArray.rotateOffsetY(offset: Int) {
            val x = this[offset + 0]
            val z = this[offset + 2]

            this[offset + 0] = -z + 1.0f // translates to origin and back; same as -(z-0.5f) + 0.5f
            this[offset + 2] = x
        }

        for (c in 0 until count) {
            for (i in 0 until 4) {
                rotateOffsetY(i * 3)
            }
        }
    }

    private fun FloatArray.rotateY(direction: Directions): FloatArray {
        if (y == 0) return this

        rotateY(y)
        if (direction.axis == Axes.Y) {
            return pushRight(3, if (direction.negative) -y else y)
        }

        if ((direction.axis == Axes.Z && (y == 2 || y == 3))) {
            return pushRight(3, if (direction.negative) -1 else 1)
        } else if (direction.axis == Axes.X && (y == 1 || y == 2)) {
            return pushRight(3, if (direction.negative) 1 else -1)
        }
        return this
    }


    override fun load(textures: TextureManager) {
        if (model.elements == null) return

        for (element in model.elements) {
            for ((_, face) in element.faces) {
                face.load(model, textures)
            }
        }
    }

    private fun rotatedY(direction: Directions, rotated: Directions): Int {
        if (direction.axis != Axes.Y) return 0
        return if (direction.negative) -y else y
    }

    private fun rotatedX(direction: Directions, rotated: Directions): Int {
        if (direction.axis == Axes.X) {
            return if (direction.negative) -x else x
        }
        if (direction == Directions.NORTH || rotated == Directions.NORTH) return 2
        return 0
    }

    private fun getTextureRotation(direction: Directions, rotated: Directions): Int {
        if (x == 0 && y == 0) return 0

        if (x == 0) {
            return rotatedY(direction, rotated)
        }
        if (y == 0) {
            return rotatedX(direction, rotated)
        }
        return rotatedX(direction, direction.rotateX(x)) + rotatedY(direction.rotateX(x), rotated)
    }


    private fun FaceUV.rotateLeft(): FaceUV {
        return FaceUV(Vec2(-(start.y - 0.5f) + 0.5f, end.x), Vec2(-(end.y - 0.5f) + 0.5f, start.x))
    }

    override fun bake(): BakedModel? {
        if (model.elements == null) return null

        val bakedFaces: Array<MutableList<BakedFace>> = Array(Directions.SIZE) { mutableListOf() }
        val properties: Array<MutableList<FaceProperties>> = Array(Directions.SIZE) { mutableListOf() }

        for (element in model.elements) {
            for ((direction, face) in element.faces) {
                val texture = face.loadedTexture ?: continue

                val rotatedDirection = direction
                    .rotateX(this.x)
                    .rotateY(this.y)


                val positions = positions(direction, element.from, element.to)
                    .rotateX(direction)
                    .rotateY(direction.rotateX(this.x))


                var abc = face.uv ?: if (uvLock) fallbackUV(rotatedDirection, positions.start(), positions.end()) else fallbackUV(direction, element.from, element.to)

                if (uvLock && face.uv != null) {
                    if (direction.axis == Axes.X) {
                        for (x in 0 until x) {
                            abc = abc.rotateLeft()
                        }
                    }
                    if (direction.axis == Axes.Y) {
                        for (y in 0 until y) {
                            abc = abc.rotateLeft()
                        }
                    }
                }

                var uv = abc.toArray(rotatedDirection, face.rotation)

                if (!uvLock) {
                    val rotation = getTextureRotation(direction, rotatedDirection)
                    uv = uv.pushRight(2, rotation)
                }
                val shade = rotatedDirection.shade

                val faceProperties = positions.properties(rotatedDirection, texture)
                val bakedFace = BakedFace(positions, uv, shade, face.tintIndex, if (faceProperties == null) null else rotatedDirection, texture, faceProperties)

                bakedFaces[rotatedDirection.ordinal] += bakedFace
                properties[rotatedDirection.ordinal] += faceProperties ?: continue
            }
        }

        return BakedModel(bakedFaces.compact(), properties.compactProperties(), null) // TODO
    }

    private fun FloatArray.start(): Vec3 {
        return Vec3(this[0], this[1], this[2])
    }

    private fun FloatArray.end(): Vec3 {
        return Vec3(this[6], this[7], this[8])
    }

    fun FloatArray.properties(direction: Directions, texture: Texture): FaceProperties? {
        // TODO: Bad code?

        val a = direction.axis.ordinal
        val b = this[a]
        if ((direction.negative && b != 0.0f) || (!direction.negative && b != 1.0f)) return null

        return FaceProperties(
            start = magic(0, a),
            end = magic(6, a),
            transparency = texture.transparency,
        )
    }

    private fun FloatArray.magic(offset: Int, index: Int): Vec2 {
        return when (index) {
            0 -> Vec2(this[offset + 1], this[offset + 2])
            1 -> Vec2(this[offset + 0], this[offset + 2])
            2 -> Vec2(this[offset + 0], this[offset + 1])
            else -> Broken()
        }
    }

    companion object {
        const val ROTATION_STEP = 90

        fun Int.rotation(): Int {
            var rotation = this

            if (rotation % ROTATION_STEP != 0) throw IllegalArgumentException("Invalid rotation: $rotation")
            rotation /= ROTATION_STEP
            return rotation and 0x03
        }

        fun deserialize(model: BlockModel, data: JsonObject): SingleBlockStateApply {
            val uvLock = data["uvlock"]?.toBoolean() ?: false
            val x = data["x"]?.toInt()?.rotation() ?: 0
            val y = data["y"]?.toInt()?.rotation() ?: 0

            return SingleBlockStateApply(model, uvLock, x, y)
        }

        fun deserialize(loader: BlockLoader, data: JsonObject): SingleBlockStateApply? {
            val model = loader.loadBlock(data["model"].toString().toResourceLocation()) ?: return null

            return deserialize(model, data)
        }

        val Directions.shade: Float
            get() = when (this) {
                Directions.UP -> 1.0f
                Directions.DOWN -> 0.5f
                Directions.NORTH, Directions.SOUTH -> 0.8f
                Directions.WEST, Directions.EAST -> 0.6f
            }

        fun fallbackUV(direction: Directions, from: Vec3, to: Vec3): FaceUV {
            return when (direction) {
                // @formatter:off
                Directions.DOWN ->  FaceUV(from.x,      1.0f - from.z,   to.x,             1.0f - to.z)
                Directions.UP ->    FaceUV(from.x,      to.z,            to.x,             from.z     )
                Directions.NORTH -> FaceUV(1.0f - to.x, 1.0f - from.y,   1.0f - from.x,    1.0f - to.y)
                Directions.SOUTH -> FaceUV(from.x,      1.0f - from.y,   to.x,             1.0f - to.y)
                Directions.WEST ->  FaceUV(from.z,      1.0f - from.y,   to.z,             1.0f - to.y)
                Directions.EAST ->  FaceUV(1.0f - to.z, 1.0f - from.y,   1.0f - from.z,    1.0f - to.y)
                // @formatter:on
            }
        }
    }
}
