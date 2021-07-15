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

package de.bixilon.minosoft.gui.rendering.hud.atlas

import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import glm_.glm
import glm_.vec2.Vec2i

data class Vec2Binding(
    val start: Vec2i,
    val end: Vec2i,
) {
    val size: Vec2i = glm.abs(start - end)

    companion object {

        fun deserialize(json: Any): Vec2Binding {
            check(json is Map<*, *>)

            return Vec2Binding(json["start"]!!.listCast<Int>()!!.let { Vec2i(it[0], it[1]) }, json["end"]!!.listCast<Int>()!!.let { Vec2i(it[0], it[1]) })
        }
    }
}
