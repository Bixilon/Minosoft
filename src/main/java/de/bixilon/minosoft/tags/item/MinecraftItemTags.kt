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

package de.bixilon.minosoft.tags.item

import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.tags.MinecraftTagTypes
import de.bixilon.minosoft.tags.TagList
import de.bixilon.minosoft.tags.TagManager

object MinecraftItemTags {

    fun Item.isIn(tags: TagManager, name: ResourceLocation?): Boolean {
        return tags.isIn(MinecraftTagTypes.ITEM, name, this)
    }

    fun Item.isIn(tags: TagList<RegistryItem>?, name: ResourceLocation?): Boolean {
        return tags?.isIn(name, this) ?: return false
    }
}
