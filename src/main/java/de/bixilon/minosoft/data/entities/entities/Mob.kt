/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3

abstract class Mob(connection: PlayConnection, entityType: EntityType, position: Vec3, rotation: EntityRotation) : LivingEntity(connection, entityType, position, rotation) {

    private fun getMobFlags(bitMask: Int): Boolean {
        return entityMetaData.sets.getBitMask(EntityMetaDataFields.MOB_FLAGS, bitMask)
    }

    @get:EntityMetaDataFunction(name = "Is no ai")
    val isNoAi: Boolean
        get() = getMobFlags(0x01)

    @get:EntityMetaDataFunction(name = "Is left handed")
    val isLeftHanded: Boolean
        get() = getMobFlags(0x02)

    @get:EntityMetaDataFunction(name = "Is aggressive")
    open val isAggressive: Boolean
        get() = getMobFlags(0x04)
}
