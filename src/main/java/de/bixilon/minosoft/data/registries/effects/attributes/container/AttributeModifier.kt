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
package de.bixilon.minosoft.data.registries.effects.attributes.container

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.data.registries.effects.attributes.AttributeOperations
import java.util.*

data class AttributeModifier(
    val name: String? = null,
    val uuid: UUID,
    val amount: Double,
    val operation: AttributeOperations,
) {
    override fun toString(): String {
        return name ?: uuid.toString()
    }

    companion object {
        fun deserialize(data: Map<String, Any>): AttributeModifier {
            return AttributeModifier(
                name = data["name"].unsafeCast(),
                uuid = data["uuid"].toString().toUUID(),
                amount = data["amount"].unsafeCast(),
                operation = AttributeOperations[data["operation"].unsafeCast<String>()],
            )
        }
    }
}
