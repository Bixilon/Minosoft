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

package de.bixilon.minosoft.data.registries.versions

import de.bixilon.minosoft.data.entities.block.FlowerPotBlockEntity
import de.bixilon.minosoft.data.registries.blocks.entites.BlockEntityType
import de.bixilon.minosoft.modding.event.events.loading.RegistriesLoadEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

object MinecraftRegistryFixer {


    fun register(connection: PlayConnection) {
        connection.register(CallbackEventListener.of<RegistriesLoadEvent> {
            if (it.state != RegistriesLoadEvent.States.POST) {
                return@of
            }
            // add minecraft:flower_pot as block entity, even if it's not a real entity, but we need it for setting the flower type (in earlier versions of the game)

            connection.registries.blockEntityTypeRegistry[FlowerPotBlockEntity] = BlockEntityType(FlowerPotBlockEntity.RESOURCE_LOCATION, setOf(connection.registries.blockRegistry[FlowerPotBlockEntity]!!), FlowerPotBlockEntity)
        })
    }
}
