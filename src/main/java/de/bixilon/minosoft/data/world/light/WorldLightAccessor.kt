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

package de.bixilon.minosoft.data.world.light

import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.RenderConstants

class WorldLightAccessor(
    private val world: World,
) : LightAccessor {
    override fun getSkyLight(blockPosition: BlockPosition): Int {
        if (RenderConstants.DISABLE_LIGHTING) {
            return 15
        }
        return world.chunks[blockPosition.getChunkPosition()]?.lightAccessor?.getSkyLight(blockPosition) ?: 0
    }

    override fun getBlockLight(blockPosition: BlockPosition): Int {
        return world.chunks[blockPosition.getChunkPosition()]?.lightAccessor?.getBlockLight(blockPosition) ?: 0
    }
}
