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
package de.bixilon.minosoft.data.entities.entities.boss.enderdragon

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class EndCrystal(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, position, rotation) {

    @get:SynchronizedEntityData(name = "Beam target")
    val beamTarget: Vec3i?
        get() = data.sets.getBlockPosition(EntityDataFields.END_CRYSTAL_BEAM_TARGET)

    @SynchronizedEntityData(name = "Show bottom")
    val showBottom: Boolean
        get() = data.sets.getBoolean(EntityDataFields.END_CRYSTAL_SHOW_BOTTOM)


    companion object : EntityFactory<EndCrystal> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("end_crystal")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): EndCrystal {
            return EndCrystal(connection, entityType, position, rotation)
        }
    }
}
