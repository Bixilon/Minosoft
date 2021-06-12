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

package de.bixilon.minosoft.data.entities.block.piston

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.entities.block.BlockActionEntity
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.block.BlockEntityFactory
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

open class PistonBlockEntity(connection: PlayConnection) : BlockEntity(connection), BlockActionEntity {
    var state: PistonStates = PistonStates.PULL
        private set
    var direction: Directions = Directions.NORTH
        private set

    override fun setBlockActionData(data1: Byte, data2: Byte) {
        state = PistonStates[data1.toInt()]
        direction = Directions[data2.toInt()]
    }

    companion object : BlockEntityFactory<PistonBlockEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:piston")

        override fun build(connection: PlayConnection): PistonBlockEntity {
            return PistonBlockEntity(connection)
        }
    }

    enum class PistonStates {
        PUSH,
        PULL,
        ;

        companion object : ValuesEnum<PistonStates> {
            override val VALUES: Array<PistonStates> = values()
            override val NAME_MAP: Map<String, PistonStates> = KUtil.getEnumValues(VALUES)

        }
    }
}
