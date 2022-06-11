/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks.types

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.materials.DefaultMaterials
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

open class DoorBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : DoubleSizeBlock(resourceLocation, registries, data) {

    override fun getPlacementState(connection: PlayConnection, target: BlockTarget): BlockState? {
        TODO()
    }

    override fun onUse(connection: PlayConnection, target: BlockTarget, hand: Hands, itemStack: ItemStack?): InteractionResults {
        if (target.blockState.material.resourceLocation == DefaultMaterials.METAL) {
            return InteractionResults.CONSUME
        }

        connection.world[target.blockPosition] = target.blockState.cycle(BlockProperties.DOOR_OPEN)

        return InteractionResults.SUCCESS
    }

    companion object : BlockFactory<DoorBlock> {
        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): DoorBlock {
            return DoorBlock(resourceLocation, registries, data)
        }
    }
}
