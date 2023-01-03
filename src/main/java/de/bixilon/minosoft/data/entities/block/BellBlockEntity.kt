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

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class BellBlockEntity(connection: PlayConnection) : BlockEntity(connection), BlockActionEntity {
    var shakingDirection: Directions = Directions.NORTH
        private set

    override fun setBlockActionData(data1: Byte, data2: Byte) {
        shakingDirection = Directions[data2.toInt()]
    }

    companion object : BlockEntityFactory<BellBlockEntity> {
        override val identifier: ResourceLocation = KUtil.minecraft("bell")

        override fun build(connection: PlayConnection): BellBlockEntity {
            return BellBlockEntity(connection)
        }
    }
}
