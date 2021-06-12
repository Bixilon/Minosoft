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
package de.bixilon.minosoft.data.entities.entities.animal.horse

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.entities.entities.animal.Animal
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d
import java.util.*

abstract class AbstractHorse(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, position, rotation) {

    private fun getAbstractHorseFlag(bitMask: Int): Boolean {
        return entityMetaData.sets.getBitMask(EntityMetaDataFields.ABSTRACT_HORSE_FLAGS, bitMask)
    }

    @get:EntityMetaDataFunction(name = "Is tame")
    val isTame: Boolean
        get() = getAbstractHorseFlag(0x02)

    @get:EntityMetaDataFunction(name = "Is saddled")
    val isSaddled: Boolean
        get() = getAbstractHorseFlag(0x04)

    @EntityMetaDataFunction(name = "Has bred")
    fun hasBred(): Boolean {
        return getAbstractHorseFlag(0x08)
    }

    @get:EntityMetaDataFunction(name = "Is eating")
    val isEating: Boolean
        get() = getAbstractHorseFlag(0x10)

    @get:EntityMetaDataFunction(name = "Is rearing")
    val isRearing: Boolean
        get() = getAbstractHorseFlag(0x20)

    @get:EntityMetaDataFunction(name = "Is mouth open")
    val isMouthOpen: Boolean
        get() = getAbstractHorseFlag(0x40)

    @get:EntityMetaDataFunction(name = "Owner UUID")
    val owner: UUID?
        get() = entityMetaData.sets.getUUID(EntityMetaDataFields.ABSTRACT_HORSE_OWNER_UUID)
}
