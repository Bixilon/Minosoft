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

class Goat(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val isScreaming: Boolean
        get() = data.getBoolean(SCREAMING_DATA, false)

    @get:SynchronizedEntityData
    val hasLeftHorn: Boolean
        get() = data.getBoolean(LEFT_HORN_DATA, false)

    @get:SynchronizedEntityData
    val hasRightHorn: Boolean
        get() = data.getBoolean(RIGHT_HORN_DATA, false)

    companion object : EntityFactory<Goat> {
        override val identifier: ResourceLocation = KUtil.minecraft("goat")
        private val SCREAMING_DATA = EntityDataField("GOAT_IS_SCREAMING")
        private val LEFT_HORN_DATA = EntityDataField("LEFT_HORN")
        private val RIGHT_HORN_DATA = EntityDataField("RIGHT_HORN")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Goat {
            return Goat(connection, entityType, data, position, rotation)
        }
    }
}
