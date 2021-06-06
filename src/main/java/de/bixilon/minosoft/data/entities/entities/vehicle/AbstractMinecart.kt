/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d

abstract class AbstractMinecart(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Shaking power")
    val shakingPower: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.MINECART_HURT)

    @get:EntityMetaDataFunction(name = "Shaking direction")
    val shakingDirection: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.MINECART_HURT_DIRECTION)

    @get:EntityMetaDataFunction(name = "Shaking multiplier")
    val shakingMultiplier: Float
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.MINECART_DAMAGE_TAKEN).toFloat()

    @get:EntityMetaDataFunction(name = "Block id")
    val blockId: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.MINECART_BLOCK_ID)

    @get:EntityMetaDataFunction(name = "Block Y offset")
    val blockYOffset: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.MINECART_BLOCK_Y_OFFSET)

    @get:EntityMetaDataFunction(name = "Is showing block")
    val isShowingBlock: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.MINECART_SHOW_BLOCK)
}
