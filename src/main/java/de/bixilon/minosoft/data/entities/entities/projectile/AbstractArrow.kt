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
package de.bixilon.minosoft.data.entities.entities.projectile

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

abstract class AbstractArrow(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Projectile(connection, entityType, position, rotation) {

    private fun getAbstractArrowFlag(bitMask: Int): Boolean {
        return data.sets.getBitMask(EntityDataFields.ABSTRACT_ARROW_FLAGS, bitMask)
    }

    @get:SynchronizedEntityData(name = "Is critical")
    val isCritical: Boolean
        get() = getAbstractArrowFlag(0x01)

    @get:SynchronizedEntityData(name = "Is no clip")
    val isNoClip: Boolean
        get() = getAbstractArrowFlag(0x02)

    @get:SynchronizedEntityData(name = "Piercing level")
    val piercingLevel: Byte
        get() = data.sets.getByte(EntityDataFields.ABSTRACT_ARROW_PIERCE_LEVEL)

    @get:SynchronizedEntityData(name = "Owner UUID")
    val ownerUUID: UUID?
        get() = data.sets.getUUID(EntityDataFields.ABSTRACT_ARROW_OWNER_UUID)
}
