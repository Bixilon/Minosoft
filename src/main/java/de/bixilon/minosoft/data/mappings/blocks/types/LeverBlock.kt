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
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.blocks.BlockUsages
import de.bixilon.minosoft.data.mappings.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.mappings.versions.Registries
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.gui.rendering.input.camera.RaycastHit
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3i

open class LeverBlock(resourceLocation: ResourceLocation, mappings: Registries, data: JsonObject) : Block(resourceLocation, mappings, data) {

    override fun getPlacementState(connection: PlayConnection, raycastHit: RaycastHit): BlockState? {
        TODO()
    }

    override fun use(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, raycastHit: RaycastHit, hands: Hands, itemStack: ItemStack?): BlockUsages {
        connection.world[blockPosition] = blockState.cycle(BlockProperties.POWERED)

        return BlockUsages.SUCCESS
    }
}
