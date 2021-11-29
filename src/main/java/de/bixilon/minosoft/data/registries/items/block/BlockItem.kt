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

package de.bixilon.minosoft.data.registries.items.block

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.input.camera.hit.BlockRaycastHit
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

open class BlockItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : Item(resourceLocation, registries, data) {
    val block: Block? = null

    init {
        this::block.inject(data["block"])
    }

    override fun interactBlock(connection: PlayConnection, raycastHit: BlockRaycastHit, hand: Hands, itemStack: ItemStack): InteractionResults {
        if (!connection.player.gamemode.canBuild) {
            return InteractionResults.PASS
        }

        val placePosition = raycastHit.blockPosition + raycastHit.hitDirection
        if (!connection.world.isPositionChangeable(placePosition)) {
            return InteractionResults.PASS
        }

        connection.world[placePosition]?.let {
            if (!it.material.replaceable) {
                return InteractionResults.PASS
            }
        }
        if (connection.world.getBlockEntity(placePosition) != null && !connection.player.isSneaking) {
            return InteractionResults.PASS
        }


        var placeBlockState: BlockState = block!!.defaultState
        try {
            placeBlockState = block.getPlacementState(connection, raycastHit) ?: return InteractionResults.PASS
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        val collisionShape = placeBlockState.collisionShape + placePosition
        for (entity in connection.world.entities) {
            if (entity.isInvisible) {
                continue
            }
            val aabb = entity.aabb

            if (collisionShape.intersect(aabb)) {
                return InteractionResults.CONSUME
            }
        }


        connection.world[placePosition] = placeBlockState

        if (connection.player.gamemode != Gamemodes.CREATIVE) {
            itemStack.count--
        }

        placeBlockState.placeSoundEvent?.let {
            connection.world.playSoundEvent(it, placePosition, placeBlockState.soundEventVolume, placeBlockState.soundEventPitch)
        }
        return InteractionResults.SUCCESS
    }
}
