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

package de.bixilon.minosoft.data.registries.blocks.properties.list

import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty

class MapPropertyList : BlockPropertyList {
    private var properties: MutableMap<String, BlockProperty<*>> = HashMap(0, 0.1f)

    override fun get(name: String): BlockProperty<*>? {
        return properties[name]
    }

    fun register(property: BlockProperty<*>) {
        properties.put(property.name, property)?.let { throw IllegalArgumentException("Block property was replaced: $property") }
    }

    operator fun plusAssign(property: BlockProperty<*>) = register(property)


    fun shrink(): BlockPropertyList {
        if (properties.isEmpty()) return EmptyPropertyList
        return this
    }
}
