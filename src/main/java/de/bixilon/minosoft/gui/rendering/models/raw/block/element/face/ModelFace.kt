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

package de.bixilon.minosoft.gui.rendering.models.raw.block.element.face

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.raw.block.element.ModelElement.Companion.BLOCK_SIZE
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import java.util.*

data class ModelFace(
    val texture: String,
    val uv: FaceUV,
    val rotation: Int,
    val cullface: Directions?,
    val tintIndex: Int = -1,
) {

    companion object {

        fun deserialize(data: JsonObject): ModelFace {
            val texture = data["texture"].toString()

            val rawUV = data["uv"]?.listCast<Number>()
            val uv = FaceUV(
                start = rawUV?.let { Vec2(rawUV[0], rawUV[1]) / BLOCK_SIZE } ?: Vec2(0.0f),
                end = rawUV?.let { Vec2(rawUV[0], rawUV[1]) / BLOCK_SIZE } ?: Vec2(1.0f),
            )

            val rotation = data["rotation"]?.toInt() ?: 0
            val cullface = data["cullface"]?.toString()?.let { if (it == "none") null else Directions[it] }
            val tintIndex = data["tintindex"]?.toInt() ?: TintManager.DEFAULT_TINT_INDEX

            return ModelFace(texture, uv, rotation, cullface, tintIndex)
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
    }
}
