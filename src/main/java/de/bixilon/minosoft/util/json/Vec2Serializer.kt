/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import glm_.vec2.Vec2

object Vec2Serializer {
    @FromJson
    fun fromJson(json: List<Float>): Vec2 {
        return Vec2(json[0], json[1])
    }

    @ToJson
    fun toJson(vec2: Vec2): List<Float> {
        return listOf(vec2.x, vec2.y)
    }
}
