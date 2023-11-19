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

package de.bixilon.minosoft.data.registries.factory

import de.bixilon.minosoft.data.registries.identified.AliasedIdentified
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation

open class DefaultFactory<T : Identified>(private vararg val factories: T) : Iterable<T> {
    private val map: MutableMap<ResourceLocation, T> = mutableMapOf()

    val size: Int = factories.size

    init {
        for (factory in factories) {
            map[factory.identifier] = factory
            if (factory is AliasedIdentified) {
                for (resourceLocation in factory.identifiers) {
                    map[resourceLocation] = factory
                }
            }
        }
    }

    fun add(factory: T) {
        map[factory.identifier] = factory
    }

    fun remove(name: ResourceLocation) {
        map -= name
    }

    fun remove(factory: Identified) {
        map -= factory.identifier
    }

    operator fun get(identifier: ResourceLocation?): T? {
        return map[identifier]
    }

    operator fun get(index: Int): T {
        return factories[index]
    }

    override fun iterator(): Iterator<T> {
        return factories.iterator()
    }
}
