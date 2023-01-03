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

package de.bixilon.minosoft.data.entities.block

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class BeaconBlockEntity(connection: PlayConnection) : BlockEntity(connection), BlockActionEntity {

    override fun setBlockActionData(data1: Byte, data2: Byte) {
        // no data used, just recalculates the beam
    }

    override fun updateNBT(nbt: Map<String, Any>) {
        // ToDO: {Secondary: -1, Paper.Range: -1.0D, Primary: -1, x: -90, y: 4, Levels: 0, z: 212, id: "minecraft:beacon"}
    }

    companion object : BlockEntityFactory<BeaconBlockEntity> {
        override val identifier: ResourceLocation = ResourceLocation("minecraft:beacon")

        override fun build(connection: PlayConnection): BeaconBlockEntity {
            return BeaconBlockEntity(connection)
        }
    }
}
