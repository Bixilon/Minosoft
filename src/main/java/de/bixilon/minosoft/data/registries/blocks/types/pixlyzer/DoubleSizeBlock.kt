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
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.Halves
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.SimpleBlockState
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class DoubleSizeBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : PixLyzerBlock(resourceLocation, registries, data) {

    override fun onBreak(connection: PlayConnection, blockPosition: Vec3i, blockState: BlockState, blockEntity: BlockEntity?) {
        if (blockState !is SimpleBlockState) return
        if (blockState.properties[BlockProperties.STAIR_HALF] == Halves.LOWER) {
            connection.world.forceSetBlockState(blockPosition + Directions.UP, null, check = true)
        } else {
            connection.world.forceSetBlockState(blockPosition + Directions.DOWN, null, check = true)
        }
    }

    override fun onPlace(connection: PlayConnection, blockPosition: Vec3i, blockState: BlockState) {
        if (blockState !is SimpleBlockState) return
        if (blockState.properties[BlockProperties.STAIR_HALF] == Halves.LOWER) {
            connection.world.forceSetBlockState(blockPosition + Directions.UP, blockState.withProperties(BlockProperties.STAIR_HALF to Halves.UPPER), check = true)
        } else {
            connection.world.forceSetBlockState(blockPosition + Directions.DOWN, blockState.withProperties(BlockProperties.STAIR_HALF to Halves.LOWER), check = true)
        }
    }
}
