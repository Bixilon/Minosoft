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
package de.bixilon.minosoft.data.entities.entities.projectile

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class FishingBobber(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Projectile(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val hookedEntityId: Int?
        get() = data.get(HOOKED_ENTITY_DATA, null)

    @get:SynchronizedEntityData
    val isCatchable: Boolean
        get() = data.getBoolean(CATCHABLE_DATA, false)


    companion object : EntityFactory<FishingBobber> {
        override val identifier: ResourceLocation = ResourceLocation("fishing_bobber")
        private val HOOKED_ENTITY_DATA = EntityDataField("FISHING_HOOK_HOOKED_ENTITY")
        private val CATCHABLE_DATA = EntityDataField("FISHING_HOOK_CATCHABLE")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): FishingBobber {
            return FishingBobber(connection, entityType, data, position, rotation)
        }
    }
}
