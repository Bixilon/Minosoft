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

package de.bixilon.minosoft.data.mappings.blocks.types

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.blocks.BlockUsages
import de.bixilon.minosoft.data.mappings.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.mappings.blocks.properties.Halves
import de.bixilon.minosoft.data.mappings.materials.DefaultMaterials
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.gui.rendering.input.camera.RaycastHit
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3i

open class DoorBlock(resourceLocation: ResourceLocation, mappings: VersionMapping, data: JsonObject) : Block(resourceLocation, mappings, data) {


    override fun getPlacementState(connection: PlayConnection, raycastHit: RaycastHit): BlockState {
        TODO()
    }

    override fun onBreak(connection: PlayConnection, blockPosition: Vec3i, blockState: BlockState, blockEntity: BlockEntity?) {
        if (blockState.properties[BlockProperties.STAIR_HALF] == Halves.LOWER) {
            connection.world.forceSetBlock(blockPosition + Directions.UP, null)
        } else {
            connection.world.forceSetBlock(blockPosition + Directions.DOWN, null)
        }
    }

    override fun onPlace(connection: PlayConnection, blockPosition: Vec3i, blockState: BlockState) {
        if (blockState.properties[BlockProperties.STAIR_HALF] == Halves.LOWER) {
            connection.world.forceSetBlock(blockPosition + Directions.UP, blockState.withProperties(BlockProperties.STAIR_HALF to Halves.UPPER))
        } else {
            connection.world.forceSetBlock(blockPosition + Directions.DOWN, blockState.withProperties(BlockProperties.STAIR_HALF to Halves.LOWER))
        }
    }

    override fun use(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, raycastHit: RaycastHit, hands: Hands, itemStack: ItemStack?): BlockUsages {
        if (blockState.material.resourceLocation == DefaultMaterials.METAL) {
            return BlockUsages.CONSUME
        }

        val nextBlockState = blockState.cycle(BlockProperties.DOOR_OPEN)

        connection.world.setBlock(blockPosition, nextBlockState)

        return BlockUsages.SUCCESS
    }

}
