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
package de.bixilon.minosoft.data.mappings.effects.attributes

import com.google.gson.JsonObject
import de.bixilon.minosoft.util.Util
import java.util.*

data class StatusEffectAttribute(
    val name: String,
    val uuid: UUID,
    val amount: Double,
    val operation: StatusEffectOperations,
) {
    override fun toString(): String {
        return name
    }

    companion object {
        fun deserialize(data: JsonObject): StatusEffectAttribute {
            return StatusEffectAttribute(
                name = data["name"].asString,
                uuid = Util.getUUIDFromString(data["uuid"].asString),
                amount = data["amount"].asDouble,
                operation = StatusEffectOperations.NAME_MAP[data["operation"].asString]!!,
            )
        }
    }
}
