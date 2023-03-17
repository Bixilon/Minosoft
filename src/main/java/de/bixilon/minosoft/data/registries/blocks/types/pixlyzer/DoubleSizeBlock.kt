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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.handler.entity.BlockBreakHandler
import de.bixilon.minosoft.data.registries.blocks.handler.entity.BlockPlaceHandler
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.Halves
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class DoubleSizeBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : PixLyzerBlock(resourceLocation, registries, data), BlockBreakHandler, BlockPlaceHandler {

    override fun onBreak(connection: PlayConnection, position: Vec3i, state: BlockState, entity: BlockEntity?) {
        if (state !is PropertyBlockState) return
        if (state.properties[BlockProperties.STAIR_HALF] == Halves.LOWER) {
            connection.world[position + Directions.UP] = null
        } else {
            connection.world[position + Directions.DOWN] = null
        }
    }

    override fun onPlace(connection: PlayConnection, position: Vec3i, state: BlockState, entity: BlockEntity?) {
        if (state !is PropertyBlockState) return
        if (state.properties[BlockProperties.STAIR_HALF] == Halves.LOWER) {
            connection.world[position + Directions.UP] = state.withProperties(BlockProperties.STAIR_HALF to Halves.UPPER)
        } else {
            connection.world[position + Directions.DOWN] = state.withProperties(BlockProperties.STAIR_HALF to Halves.LOWER)
        }
    }
}
