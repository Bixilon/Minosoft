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

package de.bixilon.minosoft.data.entities.block

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class StructureBlockBlockEntity(connection: PlayConnection) : BlockEntity(connection) {

    override fun updateNBT(nbt: Map<String, Any>) {
        // ToDo: {mirror: "NONE", metadata: "asd", ignoreEntities: 1b, powered: 0b, seed: 0l, author: "Bixilon", rotation: "NONE", posX: 0, mode: "DATA", posY: 1, sizeX: 0, integrity: 1.0F, posZ: 0, showair: 0b, x: -102, name: "", y: 4, z: 212, id: "minecraft:structure_block", sizeY: 0, sizeZ: 0, showboundingbox: 1b}
    }

    companion object : BlockEntityFactory<StructureBlockBlockEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:structure_block")

        override fun build(connection: PlayConnection): StructureBlockBlockEntity {
            return StructureBlockBlockEntity(connection)
        }
    }
}
