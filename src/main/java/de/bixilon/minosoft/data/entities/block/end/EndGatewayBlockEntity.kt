/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.block.end

import de.bixilon.minosoft.data.entities.block.BlockActionEntity
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.block.BlockEntityFactory
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class EndGatewayBlockEntity(session: PlaySession) : BlockEntity(session), BlockActionEntity {

    override fun setBlockActionData(type: Int, data: Int) {
        // just emits the beacon like beam
    }

    override fun updateNBT(nbt: Map<String, Any>) {
    }

    companion object : BlockEntityFactory<EndGatewayBlockEntity> {
        override val identifier: ResourceLocation = minecraft("end_gateway")

        override fun build(session: PlaySession): EndGatewayBlockEntity {
            return EndGatewayBlockEntity(session)
        }
    }
}
