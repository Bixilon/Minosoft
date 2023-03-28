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

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.DirectionUtil.rotateY
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.compact
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.positions
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.pushRight
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.SideSize
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

data class SingleBlockStateApply(
    val model: BlockModel,
    val uvLock: Boolean = false,
    val x: Int = 0,
    val y: Int = 0,
) : BlockStateApply {


    private fun FloatArray.rotateY(count: Int) {
        if (count == 0) return

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


    override fun bake(textures: TextureManager): BakedModel? {
        if (model.elements == null) return null

        val bakedFaces: Array<MutableList<BakedFace>> = Array(Directions.SIZE) { mutableListOf() }
        val sizes: MutableMap<Directions, MutableList<SideSize.FaceSize>> = EnumMap(Directions::class.java)

        for (element in model.elements) {
            for ((direction, face) in element.faces) {
                val texture = face.createTexture(model, textures)
                var rotatedDirection = direction
                if (y != 0) {
                    rotatedDirection = rotatedDirection.rotateY(this.y)
                }


                var positions = positions(direction, element.from, element.to)
                positions.rotateY(y)
                if (direction.axis == Axes.Y) {
                    positions = positions.pushRight(3, if (direction.negative) -y else y)
                } else {
                    if ((direction.axis == Axes.Z && (y == 2 || y == 3))) {
                        positions = positions.pushRight(3, if (direction.negative) -1 else 1)
                    } else if (direction.axis == Axes.X && (y == 1 || y == 2)) {
                        positions = positions.pushRight(3, if (direction.negative) 1 else -1)
                    }
                }

                var uv = face.uv.toArray(rotatedDirection, face.rotation)
                if (direction.axis == Axes.Y && y != 0 && !uvLock) {
                    uv = uv.pushRight(2, if (rotatedDirection.negative) -y else y)
                }
                val shade = rotatedDirection.shade

                val bakedFace = BakedFace(positions, uv, shade, face.tintIndex, face.cull, texture)

                bakedFaces[rotatedDirection.ordinal] += bakedFace
            }
        }

        return BakedModel(bakedFaces.compact(), emptyArray(), null) // TODO
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

        fun deserialize(loader: BlockLoader, data: JsonObject): SingleBlockStateApply {
            val model = loader.loadBlock(data["model"].toString().toResourceLocation())

            return deserialize(model, data)
        }

        val Directions.shade: Float
            get() = when (this) {
                Directions.UP -> 1.0f
                Directions.DOWN -> 0.5f
                Directions.NORTH, Directions.SOUTH -> 0.8f
                Directions.WEST, Directions.EAST -> 0.6f
            }
    }
}
