/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
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
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.tags.MinecraftTagTypes
import de.bixilon.minosoft.tags.TagManager

abstract class LeveledToolItem(identifier: ResourceLocation) : ToolItem(identifier), LeveledTool {

    private fun isLevelSuitable(tagManager: TagManager, blockState: BlockState): Boolean? {
        val miningTag = this.tag ?: return null
        val blockTags = tagManager[MinecraftTagTypes.BLOCK] ?: return null
        val tag = blockTags[miningTag] ?: return null
        if (blockState.block !in tag) {
            return false
        }
        for (level in ToolLevels.REVERSED) {
            val levelTag = blockTags[level.tag ?: continue] ?: continue
            if (blockState.block in levelTag) {
                // minimum tool level required
                return this.level >= level
            }
        }
        return true
    }

    override fun isLevelSuitable(session: PlaySession, blockState: BlockState): Boolean? {
        return isLevelSuitable(session.tags, blockState) ?: isLevelSuitable(session.legacyTags, blockState)
    }
}
