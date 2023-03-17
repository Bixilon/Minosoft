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
package de.bixilon.minosoft.data.entities.entities.decoration.armorstand

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ArmorStand(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : LivingEntity(connection, entityType, data, position, rotation) {

    private fun getArmorStandFlag(bitMask: Int): Boolean {
        return data.getBitMask(FLAGS_DATA, bitMask, 0x00)
    }

    override val canRaycast: Boolean get() = super.canRaycast && !isMarker
    override val hitboxColor: RGBColor? get() = if (isMarker) null else super.hitboxColor

    @get:SynchronizedEntityData
    val isSmall: Boolean
        get() = getArmorStandFlag(0x01)

    @get:SynchronizedEntityData
    val hasArms: Boolean
        get() = getArmorStandFlag(0x04)

    @get:SynchronizedEntityData
    val hasNoBasePlate: Boolean
        get() = getArmorStandFlag(0x08)

    @get:SynchronizedEntityData
    val isMarker: Boolean
        get() = getArmorStandFlag(0x10)

    @get:SynchronizedEntityData
    val headRotation: Vec3
        get() = data.get(HEAD_ROTATION_DATA, Vec3(0.0f, 0.0f, 0.0f))

    @get:SynchronizedEntityData
    val bodyRotation: Vec3
        get() = data.get(BODY_ROTATION_DATA, Vec3(0.0f, 0.0f, 0.0f))

    @get:SynchronizedEntityData
    val leftArmRotation: Vec3
        get() = data.get(LEFT_ARM_ROTATION_DATA, Vec3(-10.0f, 0.0f, -10.0f))

    @get:SynchronizedEntityData
    val rightArmRotation: Vec3
        get() = data.get(RIGHT_ARM_ROTATION_DATA, Vec3(-15.0f, 0.0f, 10.0f))

    @get:SynchronizedEntityData
    val leftLegRotation: Vec3
        get() = data.get(LEFT_LEG_ROTATION_DATA, Vec3(-1.0f, 0.0f, -1.0f))

    @get:SynchronizedEntityData
    val rightLegRotation: Vec3
        get() = data.get(RIGHT_LEG_ROTATION_DATA, Vec3(1.0f, 0.0f, 1.0f))


    companion object : EntityFactory<ArmorStand> {
        override val identifier: ResourceLocation = minecraft("armor_stand")
        val FLAGS_DATA = EntityDataField("ARMOR_STAND_FLAGS")
        private val HEAD_ROTATION_DATA = EntityDataField("ARMOR_STAND_HEAD_ROTATION")
        private val BODY_ROTATION_DATA = EntityDataField("ARMOR_STAND_BODY_ROTATION")
        private val LEFT_ARM_ROTATION_DATA = EntityDataField("ARMOR_STAND_LEFT_ARM_ROTATION")
        private val RIGHT_ARM_ROTATION_DATA = EntityDataField("ARMOR_STAND_RIGHT_ARM_ROTATION")
        private val LEFT_LEG_ROTATION_DATA = EntityDataField("ARMOR_STAND_LEFT_LEG_ROTATION", "ARMOR_STAND_LEFT_LAG_ROTATION")
        private val RIGHT_LEG_ROTATION_DATA = EntityDataField("ARMOR_STAND_RIGHT_LEG_ROTATION", "ARMOR_STAND_RIGHT_LAG_ROTATION")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): ArmorStand {
            return ArmorStand(connection, entityType, data, position, rotation)
        }
    }
}
