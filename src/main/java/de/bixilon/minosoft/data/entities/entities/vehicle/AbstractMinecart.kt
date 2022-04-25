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
package de.bixilon.minosoft.data.entities.entities.vehicle

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class AbstractMinecart(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val shakingPower: Int
        get() = data.get(SHAKING_POWER_DATA, 0)

    @get:SynchronizedEntityData
    val shakingDirection: Int
        get() = data.get(SHAKING_DIRECTION_DATA, 0)

    @get:SynchronizedEntityData
    val shakingMultiplier: Float
        get() = data.get(SHAKING_MULTIPLIER_DATA, 0.0f)

    @get:SynchronizedEntityData
    val blockId: BlockState?
        get() = connection.registries.blockStateRegistry[data.get(BLOCK_DATA, 0)]

    @get:SynchronizedEntityData
    val blockYOffset: Int
        get() = data.get(BLOCK_Y_OFFSET_DATA, 0)

    @get:SynchronizedEntityData
    val isShowingBlock: Boolean
        get() = data.getBoolean(IS_SHOWING_BLOCK_DATA, false)

    companion object {
        private val SHAKING_POWER_DATA = EntityDataField("MINECART_HURT")
        private val SHAKING_DIRECTION_DATA = EntityDataField("MINECART_HURT_DIRECTION")
        private val SHAKING_MULTIPLIER_DATA = EntityDataField("MINECART_DAMAGE_TAKEN")
        private val BLOCK_DATA = EntityDataField("MINECART_BLOCK_ID")
        private val BLOCK_Y_OFFSET_DATA = EntityDataField("MINECART_BLOCK_Y_OFFSET")
        private val IS_SHOWING_BLOCK_DATA = EntityDataField("MINECART_SHOW_BLOCK")
    }
}
