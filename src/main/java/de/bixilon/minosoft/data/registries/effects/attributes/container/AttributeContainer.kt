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

package de.bixilon.minosoft.data.registries.effects.attributes.container

import java.util.*

class AttributeContainer(
    var base: Double = 1.0,
) : Iterable<AttributeModifier> {
    private val modifier: MutableMap<UUID, AttributeModifier> = mutableMapOf()

    fun remove(modifier: UUID) {
        this.modifier -= modifier
    }

    operator fun minusAssign(modifier: UUID) = remove(modifier)
    operator fun minusAssign(modifier: AttributeModifier) = remove(modifier.uuid)

    fun add(modifier: AttributeModifier) {
        this.modifier[modifier.uuid] = modifier
    }

    operator fun plusAssign(modifier: AttributeModifier) = add(modifier)


    fun update(update: AttributeContainerUpdate) {
        this.base = update.base
        this.modifier.clear()
        for ((uuid, modifier) in update.modifier) {
            this.modifier[uuid] = modifier
        }
    }

    operator fun contains(uuid: UUID) = uuid in modifier

    override fun iterator(): Iterator<AttributeModifier> {
        return modifier.values.iterator()
    }
}
