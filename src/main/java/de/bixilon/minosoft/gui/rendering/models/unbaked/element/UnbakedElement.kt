/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3

data class UnbakedElement(
    val from: Vec3,
    val to: Vec3,
    val rotation: UnbakedElementRotation?,
    val shade: Boolean,
    val faces: Set<UnbakedElementFace>,
) {

    companion object {
        const val BLOCK_RESOLUTION = 16.0f

        operator fun invoke(data: Map<String, Any>): UnbakedElement {
            val faces: MutableSet<UnbakedElementFace> = mutableSetOf()
            val from = data["from"].toVec3() / BLOCK_RESOLUTION
            val to = data["to"].toVec3() / BLOCK_RESOLUTION

            data["faces"].asJsonObject().let {
                for ((directionString, faceData) in it) {
                    val direction = Directions[directionString]
                    val (fallbackUVStart, fallbackUVEnd) = direction.getFallbackUV(from, to)
                    faces += UnbakedElementFace(direction, faceData.unsafeCast(), fallbackUVStart, fallbackUVEnd)
                }
            }

            return UnbakedElement(
                from = from,
                to = to,
                rotation = data["rotation"]?.toJsonObject()?.let { return@let UnbakedElementRotation(data = it) },
                shade = data["shade"]?.toBoolean() ?: true,
                faces = faces,
            )
        }
    }
}
