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
import de.bixilon.minosoft.data.registries.blocks.BlockUsages
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.input.camera.hit.BlockRaycastHit
import de.bixilon.minosoft.gui.rendering.input.camera.hit.RaycastHit
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.BlockPlaceC2SP
import glm_.vec3.Vec3

open class BlockItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : Item(resourceLocation, registries, data) {
    val block: Block? = null

    init {
        this::block.inject(data["block"])
    }

    override fun use(connection: PlayConnection, raycastHit: RaycastHit, hands: Hands, itemStack: ItemStack): BlockUsages {
        if (!connection.player.gamemode.canBuild) {
            return BlockUsages.PASS
        }

        if (raycastHit !is BlockRaycastHit) {
            return super.use(connection, raycastHit, hands, itemStack)
        }

        val placePosition = raycastHit.blockPosition + raycastHit.hitDirection
        if (!connection.world.isPositionChangeable(placePosition)) {
            return BlockUsages.PASS
        }

        connection.world[placePosition]?.let {
            if (!it.material.replaceable) {
                return BlockUsages.PASS
            }
        }


        val placeBlockState = block!!.getPlacementState(connection, raycastHit) ?: return BlockUsages.PASS


        connection.world[placePosition] = placeBlockState

        if (connection.player.gamemode != Gamemodes.CREATIVE) {
            itemStack.count--
            connection.player.inventory.validate()
        }

        placeBlockState.placeSoundEvent?.let {
            connection.world.playSoundEvent(it, placePosition, placeBlockState.soundEventVolume, placeBlockState.soundEventPitch)
        }


        connection.sendPacket(BlockPlaceC2SP(
            position = placePosition,
            direction = raycastHit.hitDirection,
            cursorPosition = Vec3(raycastHit.hitPosition),
            item = connection.player.inventory.getHotbarSlot(),
            hand = Hands.MAIN_HAND,
            insideBlock = false,  // ToDo
        ))
        return BlockUsages.SUCCESS
    }
}
