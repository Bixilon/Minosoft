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
package de.bixilon.minosoft.data.entities.entities.animal

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

class Fox(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val variant: FoxVariants
        get() = FoxVariants.VALUES.getOrNull(data.get(VARIANT_DATA, FoxVariants.RED.ordinal)) ?: FoxVariants.RED

    private fun getFoxFlag(bitMask: Int): Boolean {
        return data.getBitMask(FLAGS_DATA, bitMask, 0x00)
    }

    @get:SynchronizedEntityData
    val isSitting: Boolean
        get() = getFoxFlag(0x01)

    @get:SynchronizedEntityData
    override val isSneaking: Boolean
        get() = getFoxFlag(0x04)

    @get:SynchronizedEntityData
    val isInterested: Boolean
        get() = getFoxFlag(0x08)

    @get:SynchronizedEntityData
    val isPouncing: Boolean
        get() = getFoxFlag(0x10)

    @get:SynchronizedEntityData
    override val isSleeping: Boolean
        get() = getFoxFlag(0x20)

    @get:SynchronizedEntityData
    val isWalking: Boolean
        get() = getFoxFlag(0x40)

    @get:SynchronizedEntityData
    val isDefending: Boolean
        get() = getFoxFlag(0x80)

    @get:SynchronizedEntityData
    val owner: UUID?
        get() = data.get(OWNER_DATA, null)

    @get:SynchronizedEntityData
    val trusted: UUID?
        get() = data.get(TRUSTED_DATA, null)


    enum class FoxVariants {
        RED,
        SNOW,
        ;

        companion object : ValuesEnum<FoxVariants> {
            override val VALUES: Array<FoxVariants> = values()
            override val NAME_MAP: Map<String, FoxVariants> = EnumUtil.getEnumValues(VALUES)
        }
    }

    companion object : EntityFactory<Fox> {
        override val identifier: ResourceLocation = ResourceLocation("fox")
        private val VARIANT_DATA = EntityDataField("FOX_VARIANT")
        private val FLAGS_DATA = EntityDataField("FOX_FLAGS")
        private val OWNER_DATA = EntityDataField("FOX_TRUSTED_1")
        private val TRUSTED_DATA = EntityDataField("FOX_TRUSTED_2")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Fox {
            return Fox(connection, entityType, data, position, rotation)
        }
    }
}
