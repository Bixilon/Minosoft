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

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.data.world.InChunkPosition
import de.bixilon.minosoft.data.world.World

class ChunkLightAccessor(
    val blockLightLevel: MutableMap<InChunkPosition, Byte> = mutableMapOf(),
    val skyLightLevel: MutableMap<InChunkPosition, Byte> = mutableMapOf(),
    val world: World,
) : LightAccessor {
    override fun getLightLevel(blockPosition: BlockPosition, direction: Directions): Int {
        val inChunkPosition = blockPosition.getInChunkPosition()
        val lightLevel = blockLightLevel[inChunkPosition] ?: skyLightLevel[inChunkPosition]

        if (lightLevel == null) {
            return 1
        }

        return lightLevel.toInt()
    }
}
