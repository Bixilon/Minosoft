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
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

abstract class AbstractArrow(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Projectile(connection, entityType, data, position, rotation) {

    private fun getAbstractArrowFlag(bitMask: Int): Boolean {
        return data.getBitMask(FLAGS_DATA, bitMask, 0x00)
    }

    @get:SynchronizedEntityData
    val isCritical: Boolean
        get() = getAbstractArrowFlag(0x01)

    @get:SynchronizedEntityData
    val isNoClip: Boolean
        get() = getAbstractArrowFlag(0x02)

    @get:SynchronizedEntityData
    val piercingLevel: Byte
        get() = data.get(PIERCE_LEVEL_DATA, 0)

    @get:SynchronizedEntityData
    val ownerUUID: UUID?
        get() = data.get(OWNER_DATA, null)


    override fun onAttack(attacker: Entity): Boolean {
        return false
    }

    companion object {
        private val FLAGS_DATA = EntityDataField("ABSTRACT_ARROW_FLAGS")
        private val PIERCE_LEVEL_DATA = EntityDataField("ABSTRACT_ARROW_PIERCE_LEVEL")
        private val OWNER_DATA = EntityDataField("ABSTRACT_ARROW_OWNER_UUID")
    }
}
