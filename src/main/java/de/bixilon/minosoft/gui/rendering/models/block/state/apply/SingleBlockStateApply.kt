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

    /*
    private fun FloatArray.rotateUp(count: Int): FloatArray {
        if (count == 0) return this
        val a = this.map { it - 0.5f }.toFloatArray()
        val b = when (count) {
            1 -> floatArrayOf(a[2], a[1], -a[0], a[5], a[4], -a[3], a[8], a[7], -a[6], a[11], a[10], -a[9])
            else -> this
        }
        val c = b.pushRight(3, -count)
        val d = c.map { it + 0.5f }.toFloatArray()

        return d
    }
     */

    private fun FloatArray.rotateOffset(offset: Int) {
        val x = this[offset + 0]
        val y = this[offset + 2]

        this[offset + 0] = -y + 1.0f
        this[offset + 2] = x
    }

    private fun FloatArray.rotateY(count: Int, negative: Boolean): FloatArray {
        if (count == 0) return this

        for (c in 0 until count) {
            for (i in 0 until 4) {
                rotateOffset(i * 3)
            }
        }

        return this.pushRight(3, if (negative) -count else count)
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


                var positions = positions(rotatedDirection, element.from, element.to)
                if (direction.axis == Axes.Y) {
                    positions = positions.rotateY(y, direction.negative)
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
