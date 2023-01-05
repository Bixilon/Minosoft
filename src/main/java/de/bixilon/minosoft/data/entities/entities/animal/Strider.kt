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
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class Strider(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val boostTime: Int
        get() = data.get(TIME_TO_BOOST_DATA, 0)

    @get:SynchronizedEntityData
    val isSuffocating: Boolean
        get() = data.get(IS_SUFFOCATING_DATA, false)

    @get:SynchronizedEntityData
    val hasSaddle: Boolean
        get() = data.get(HAS_SADDLE_DATA, false)


    companion object : EntityFactory<Strider> {
        override val identifier: ResourceLocation = KUtil.minecraft("strider")
        private val TIME_TO_BOOST_DATA = EntityDataField("STRIDER_TIME_TO_BOOST")
        private val IS_SUFFOCATING_DATA = EntityDataField("STRIDER_IS_SUFFOCATING")
        private val HAS_SADDLE_DATA = EntityDataField("STRIDER_HAS_SADDLE")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Strider {
            return Strider(connection, entityType, data, position, rotation)
        }
    }
}
