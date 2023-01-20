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

package de.bixilon.minosoft.data.registries.item.items.tool

import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.tool.properties.LeveledTool
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.TagsS2CP

abstract class LeveledToolItem(identifier: ResourceLocation) : ToolItem(identifier), LeveledTool {


    override fun checkTag(connection: PlayConnection, blockState: BlockState): Boolean? {
        val blockTags = connection.tags[TagsS2CP.BLOCK_TAG_RESOURCE_LOCATION] ?: return null
        val tag = blockTags[tag]?.entries ?: return null
        if (blockState.block !in tag) {
            return false
        }
        for (level in ToolLevels.REVERSED) {
            val levelTag = blockTags[level.tag] ?: continue
            if (blockState.block in levelTag.entries) {
                // minimum tool level required
                return this.level >= level
            }
        }
        return true
    }
}
