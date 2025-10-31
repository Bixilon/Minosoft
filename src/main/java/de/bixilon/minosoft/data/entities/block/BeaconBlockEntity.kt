/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class BeaconBlockEntity(session: PlaySession, position: BlockPosition, state: BlockState) : BlockEntity(session, position, state), BlockActionEntity {

    override fun setBlockActionData(type: Int, data: Int) {
        // no data used, just recalculates the beam
    }

    override fun updateNBT(nbt: Map<String, Any>) {
        // ToDO: {Secondary: -1, Paper.Range: -1.0D, Primary: -1, x: -90, y: 4, Levels: 0, z: 212, id: "minecraft:beacon"}
    }

    companion object : BlockEntityFactory<BeaconBlockEntity> {
        override val identifier = minecraft("beacon")

        override fun build(session: PlaySession, position: BlockPosition, state: BlockState) = BeaconBlockEntity(session, position, state)
    }
}
