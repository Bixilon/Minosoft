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

package de.bixilon.minosoft.tags

import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem

class TagList<T : RegistryItem>(
    private val tags: Map<ResourceLocation, Tag<T>> = emptyMap(),
) {

    operator fun get(name: ResourceLocation): Tag<T>? {
        return tags[name]
    }

    fun isIn(name: ResourceLocation?, value: T?): Boolean {
        if (name == null || value == null) {
            return false
        }
        val tag = this[name] ?: return false
        return value in tag
    }
}
