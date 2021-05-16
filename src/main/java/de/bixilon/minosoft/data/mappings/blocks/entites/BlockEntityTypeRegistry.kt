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

package de.bixilon.minosoft.data.mappings.blocks.entites

import de.bixilon.minosoft.data.mappings.blocks.types.Block
import de.bixilon.minosoft.data.mappings.registry.Registry
import de.bixilon.minosoft.data.mappings.versions.VersionMapping

class BlockEntityTypeRegistry(
    parentRegistry: BlockEntityTypeRegistry? = null,
) : Registry<BlockEntityType>(parentRegistry) {
    private lateinit var blockTypeMap: MutableMap<Block, BlockEntityType>

    fun getByBlock(block: Block): BlockEntityType? {
        val parentRegistry = super.parentRegistry as BlockEntityTypeRegistry?
        return blockTypeMap[block] ?: parentRegistry?.getByBlock(block)
    }

    override fun postInit(versionMapping: VersionMapping) {
        super.postInit(versionMapping)
        blockTypeMap = mutableMapOf()
        for ((_, type) in resourceLocationMap) {
            for (block in type.blocks) {
                blockTypeMap[block] = type
            }
        }
    }
}
