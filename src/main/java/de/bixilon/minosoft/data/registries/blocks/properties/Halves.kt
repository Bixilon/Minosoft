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

package de.bixilon.minosoft.data.registries.blocks.properties

import de.bixilon.minosoft.data.registries.blocks.properties.serializer.BlockPropertiesSerializer
import java.util.*

enum class Halves(
    vararg val aliases: Any,
) {
    UPPER("top"),
    LOWER("bottom"),
    DOUBLE,
    ;

    companion object : BlockPropertiesSerializer {
        private val NAME_MAP: Map<Any, Halves>

        init {
            val names: MutableMap<Any, Halves> = mutableMapOf()

            for (value in values()) {
                names[value.name.lowercase(Locale.getDefault())] = value
                for (alias in value.aliases) {
                    names[alias] = value
                }
            }

            NAME_MAP = names.toMap()
        }

        override fun deserialize(value: Any): Halves {
            return NAME_MAP[value] ?: throw IllegalArgumentException("No such property: $value")
        }
    }
}
