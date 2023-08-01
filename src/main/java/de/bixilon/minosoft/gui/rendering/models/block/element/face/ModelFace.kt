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

package de.bixilon.minosoft.gui.rendering.models.block.element.face

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement.Companion.BLOCK_SIZE
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.SingleBlockStateApply.Companion.rotation
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import java.util.*

data class ModelFace(
    val texture: String,
    val uv: FaceUV?,
    val rotation: Int,
    val tintIndex: Int = -1,
) {
    var loadedTexture: Texture? = null


    fun load(model: BlockModel, textures: TextureManager) {
        this.loadedTexture = model.getTexture(this.texture, textures)
    }


    fun getUV(uvLock: Boolean, from: Vec3, to: Vec3, direction: Directions, rotatedDirection: Directions, positions: FloatArray, x: Int, y: Int): FaceUV {
        if (!uvLock) {
            return this.uv ?: fallbackUV(direction, from, to)
        }
        var rotated = this.uv ?: return fallbackUV(rotatedDirection, positions.start(), positions.end())

        if (direction.axis == Axes.X && x > 0) {
            for (i in 0 until x) {
                rotated = rotated.rotateLeft()
            }
        }
        if (direction.axis == Axes.Y && y > 0) {
            for (i in 0 until y) {
                rotated = rotated.rotateLeft()
            }
        }

        return rotated
    }

    companion object {

        fun deserialize(data: JsonObject): ModelFace {
            val texture = data["texture"].toString()

            val uv = data["uv"]?.listCast<Number>()?.let {
                // auto transform (flip) y coordinate (in minosoft 0|0 is left up, not like in minecraft/opengl where it is left down)
                FaceUV(
                    start = Vec2(it[0].toFloat(), it[3].toFloat()) / BLOCK_SIZE,
                    end = Vec2(it[2].toFloat(), it[1].toFloat()) / BLOCK_SIZE,
                )
            }

            val rotation = data["rotation"]?.toInt()?.rotation() ?: 0
            val tintIndex = data["tintindex"]?.toInt() ?: TintManager.DEFAULT_TINT_INDEX

            return ModelFace(texture, uv, rotation, tintIndex)
        }

        fun deserialize(data: Map<String, JsonObject>): Map<Directions, ModelFace>? {
            val map: MutableMap<Directions, ModelFace> = EnumMap(Directions::class.java)

            for ((key, value) in data) {
                val direction = Directions[key]
                val face = deserialize(value)

                map[direction] = face
            }

            if (map.isEmpty()) return null

            return map
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

        private fun FloatArray.start(): Vec3 {
            return Vec3(this[0], this[1], this[2])
        }

        private fun FloatArray.end(): Vec3 {
            return Vec3(this[6], this[7], this[8])
        }


        private fun FaceUV.rotateLeft(): FaceUV {
            return FaceUV(Vec2(-start.y + 1.0f, end.x), Vec2(-end.y + 1.0f, start.x))
        }
    }
}
