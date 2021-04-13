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
package de.bixilon.minosoft.data.entities.entities

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

abstract class LivingEntity(connection: PlayConnection, entityType: EntityType, position: Vec3, rotation: EntityRotation) : Entity(connection, entityType, position, rotation) {

    private fun getLivingEntityFlag(bitMask: Int): Boolean {
        return entityMetaData.sets.getBitMask(EntityMetaDataFields.LIVING_ENTITY_FLAGS, bitMask)
    }

    // = isUsingItem
    @get:EntityMetaDataFunction(name = "Is hand active")
    val isHandActive: Boolean
        get() = getLivingEntityFlag(0x01)

    @get:EntityMetaDataFunction(name = "Main hand")
    open val mainHand: Hands?
        get() = if (getLivingEntityFlag(0x04)) Hands.OFF_HAND else Hands.MAIN_HAND

    @get:EntityMetaDataFunction(name = "Is auto spin attack")
    val isAutoSpinAttack: Boolean
        get() = getLivingEntityFlag(0x04)

    @get:EntityMetaDataFunction(name = "Health")
    open val health: Float
        get() {
            val meta = entityMetaData.sets.getFloat(EntityMetaDataFields.LIVING_ENTITY_HEALTH)
            return if (meta == Float.MIN_VALUE) {
                entityType.maxHealth
            } else {
                meta
            }
        }

    @get:EntityMetaDataFunction(name = "Effect color")
    val effectColor: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.LIVING_ENTITY_EFFECT_COLOR)

    @get:EntityMetaDataFunction(name = "Is effect ambient")
    val effectAmbient: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.LIVING_ENTITY_EFFECT_AMBIENCE)

    @get:EntityMetaDataFunction(name = "Arrows in entity")
    val arrowCount: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.LIVING_ENTITY_ARROW_COUNT)

    @get:EntityMetaDataFunction(name = "Absorption hearts")
    val absorptionHearts: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.LIVING_ENTITY_ABSORPTION_HEARTS)

    @get:EntityMetaDataFunction(name = "Bed location")
    val bedPosition: Vec3i?
        get() = entityMetaData.sets.getBlockPosition(EntityMetaDataFields.LIVING_ENTITY_BED_POSITION)
}
