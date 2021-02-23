/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.items

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.IdentifierDeserializer
import de.bixilon.minosoft.data.mappings.Item
import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.data.mappings.Registry
import de.bixilon.minosoft.data.mappings.versions.VersionMapping

class ItemRegistry(
    parentRegistry: Registry<Item>? = null,
    initialSize: Int = 50,
) : Registry<Item>(parentRegistry = parentRegistry, initialSize = initialSize) {
    private var flattened = false

    override fun get(id: Int): Item? {
        return if (!flattened) {
            val itemId = id ushr 16
            val itemMeta = id and 0xFFFF

            var versionItemId = itemId shl 16
            if (itemMeta > 0 && itemMeta < Short.MAX_VALUE) {
                versionItemId = versionItemId or itemMeta
            }
            return super.get(versionItemId) ?: super.get(itemId shl 16) // ignore meta data ?
        } else {
            super.get(id)
        }
    }

    override fun initialize(data: Map<ModIdentifier, JsonObject>?, mappings: VersionMapping, deserializer: IdentifierDeserializer<Item>, flattened: Boolean, metaType: MetaTypes) {
        this.flattened = flattened
        super.initialize(data, mappings, deserializer, flattened, metaType)
    }
}
