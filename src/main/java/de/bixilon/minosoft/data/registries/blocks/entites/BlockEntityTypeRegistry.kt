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

package de.bixilon.minosoft.data.registries.blocks.entites

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.Registry

class BlockEntityTypeRegistry(
    parentRegistry: BlockEntityTypeRegistry? = null,
) : Registry<BlockEntityType<*>>(parentRegistry, codec = BlockEntityType) {
    private val blockTypeMap: MutableMap<Block, BlockEntityType<*>> = HashMap()

    operator fun get(block: Block): BlockEntityType<*>? {
        val parentRegistry = super.parent as BlockEntityTypeRegistry?
        return blockTypeMap[block] ?: parentRegistry?.get(block)
    }

    override fun get(any: Any?): BlockEntityType<*>? {
        if (any is Block) {
            return get(any)
        }
        return super.get(any)
    }

    override fun addItem(resourceLocation: ResourceLocation, id: Int?, data: JsonObject, registries: Registries?): BlockEntityType<*>? {
        val item = super.addItem(resourceLocation, id, data, registries) ?: return null

        for (block in item.blocks) {
            blockTypeMap[block] = item
        }
        return item
    }
}
