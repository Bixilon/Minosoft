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
package de.bixilon.minosoft.data.entities.entities.decoration

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3d

class ArmorStand(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : LivingEntity(connection, entityType, position, rotation) {

    private fun getArmorStandFlag(bitMask: Int): Boolean {
        return entityMetaData.sets.getBitMask(EntityMetaDataFields.ARMOR_STAND_FLAGS, bitMask)
    }

    @get:EntityMetaDataFunction(name = "Is small")
    val isSmall: Boolean
        get() = getArmorStandFlag(0x01)

    @EntityMetaDataFunction(name = "Has arms")
    fun hasArms(): Boolean {
        return getArmorStandFlag(0x04)
    }

    @EntityMetaDataFunction(name = "Has no base plate")
    fun hasNoBasePlate(): Boolean {
        return getArmorStandFlag(0x08)
    }

    @get:EntityMetaDataFunction(name = "Is marker")
    val isMarker: Boolean
        get() = getArmorStandFlag(0x10)

    @get:EntityMetaDataFunction(name = "Head rotation")
    val headRotation: EntityRotation
        get() = entityMetaData.sets.getRotation(EntityMetaDataFields.ARMOR_STAND_HEAD_ROTATION)

    @get:EntityMetaDataFunction(name = "Body rotation")
    val bodyRotation: EntityRotation
        get() = entityMetaData.sets.getRotation(EntityMetaDataFields.ARMOR_STAND_BODY_ROTATION)

    @get:EntityMetaDataFunction(name = "Left arm rotation")
    val leftArmRotation: EntityRotation
        get() = entityMetaData.sets.getRotation(EntityMetaDataFields.ARMOR_STAND_LEFT_ARM_ROTATION)

    @get:EntityMetaDataFunction(name = "Right arm rotation")
    val rightArmRotation: EntityRotation
        get() = entityMetaData.sets.getRotation(EntityMetaDataFields.ARMOR_STAND_RIGHT_ARM_ROTATION)

    @get:EntityMetaDataFunction(name = "Left leg rotation")
    val leftLegRotation: EntityRotation
        get() = entityMetaData.sets.getRotation(EntityMetaDataFields.ARMOR_STAND_LEFT_LAG_ROTATION)

    @get:EntityMetaDataFunction(name = "Right leg rotation")
    val rightLegRotation: EntityRotation
        get() = entityMetaData.sets.getRotation(EntityMetaDataFields.ARMOR_STAND_RIGHT_LAG_ROTATION)


    companion object : EntityFactory<ArmorStand> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("armor_stand")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): ArmorStand {
            return ArmorStand(connection, entityType, position, rotation)
        }
    }
}
