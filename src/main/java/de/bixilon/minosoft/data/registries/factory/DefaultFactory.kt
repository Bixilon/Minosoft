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

package de.bixilon.minosoft.data.registries.factory

import de.bixilon.minosoft.data.registries.MultiResourceLocationAble
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.ResourceLocationAble

open class DefaultFactory<T : ResourceLocationAble>(private vararg val factories: T) : Iterable<T> {
    private val mapped: Map<ResourceLocation, T>

    val size: Int = factories.size

    init {
        val map: MutableMap<ResourceLocation, T> = mutableMapOf()


        for (factory in factories) {
            map[factory.resourceLocation] = factory
            if (factory is MultiResourceLocationAble) {
                for (resourceLocation in factory.resourceLocations) {
                    map[resourceLocation] = factory
                }
            }
        }

        mapped = map
    }

    operator fun get(resourceLocation: ResourceLocation): T? {
        return mapped[resourceLocation]
    }

    operator fun get(index: Int): T {
        return factories[index]
    }

    override fun iterator(): Iterator<T> {
        return factories.iterator()
    }
}
