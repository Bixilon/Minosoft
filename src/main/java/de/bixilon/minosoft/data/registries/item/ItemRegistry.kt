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

package de.bixilon.minosoft.data.registries.item

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.registries.item.factory.ItemFactories
import de.bixilon.minosoft.data.registries.item.factory.ItemFactory
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.PixLyzerItem
import de.bixilon.minosoft.data.registries.registries.registry.MetaTypes
import de.bixilon.minosoft.data.registries.registries.registry.Registry

class ItemRegistry(
    parent: Registry<Item>? = null,
    flattened: Boolean = false,
) : Registry<Item>(parent = parent, codec = PixLyzerItem, integrated = ItemFactories, flattened = flattened, metaType = MetaTypes.ITEM) {

    override fun getOrNull(id: Int): Item? {
        if (flattened) {
            return super.getOrNull(id)
        }
        val itemId = id ushr 16
        val meta = id and 0xFFFF

        var versionItemId = itemId shl 16
        if (meta > 0) {
            versionItemId = versionItemId or meta
        }
        return super.getOrNull(versionItemId) ?: super.getOrNull(itemId shl 16) // ignore meta?
    }

    operator fun <T : Item> get(factory: ItemFactory<T>): T? {
        val item = this[factory.identifier] ?: return null
        return item.unsafeCast()
    }
}
