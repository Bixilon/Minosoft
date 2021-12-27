/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.models.unbaked.element

import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.unbaked.element.UnbakedElement.Companion.BLOCK_RESOLUTION
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import glm_.vec2.Vec2

data class UnbakedElementFace(
    val direction: Directions,
    val uvStart: Vec2,
    val uvEnd: Vec2,
    val texture: String,
    val cullFace: Directions?,
    val rotation: Int,
    val tintIndex: Int,
) {
    companion object {
        operator fun invoke(direction: Directions, data: Map<String, Any>, fallbackUvStart: Vec2, fallbackUvEnd: Vec2): UnbakedElementFace {
            val uv = data["uv"]?.listCast<Number>()
            val uvStart = uv?.let { Vec2(it[0], it[1]) / BLOCK_RESOLUTION } ?: fallbackUvStart
            val uvEnd = uv?.let { Vec2(it[2], it[3]) / BLOCK_RESOLUTION } ?: fallbackUvEnd

            val cullFace = data["cullface"]?.toString()?.let {
                if (it == "none") {
                    return@let null
                }
                return@let Directions[it]
            }

            return UnbakedElementFace(
                direction = direction,
                uvStart = uvStart,
                uvEnd = uvEnd,
                texture = data["texture"].toString(),
                cullFace = cullFace,
                rotation = data["rotation"]?.toInt() ?: 0,
                tintIndex = data["tintindex"]?.toInt() ?: -1,
            )
        }
    }
}
