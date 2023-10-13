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

package de.bixilon.minosoft.data.registries.blocks.properties

import de.bixilon.kutil.enums.ValuesEnum

class EnumProperty<T : Enum<*>>(
    name: String,
    val values: ValuesEnum<T>,
    val allowed: Set<T>? = null,
) : BlockProperty<T>(name) {

    override fun parse(value: Any): T {
        val value = values[value] ?: throw IllegalArgumentException("Invalid enum value: $value")
        if (allowed != null && value !in allowed) {
            throw IllegalArgumentException("Enum value not allowed: $value")
        }

        return value
    }


    override fun equals(other: Any?): Boolean {
        if (other !is EnumProperty<*>) return false
        if (other.name != name) return false
        return other.values == values && allowed == other.allowed
    }

    override fun iterator(): Iterator<T> {
        return allowed?.iterator() ?: values.iterator()
    }
}
