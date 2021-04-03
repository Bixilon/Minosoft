/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.boss.enderdragon

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.entities.EntityFactory
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.protocol.network.Connection
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class EndCrystal(connection: Connection, entityType: EntityType, position: Vec3, rotation: EntityRotation) : Entity(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Beam target")
    val beamTarget: Vec3i?
        get() = entityMetaData.sets.getBlockPosition(EntityMetaDataFields.END_CRYSTAL_BEAM_TARGET)

    @EntityMetaDataFunction(name = "Show bottom")
    val showBottom: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.END_CRYSTAL_SHOW_BOTTOM)


    companion object : EntityFactory<EndCrystal> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("end_crystal")

        override fun build(connection: Connection, entityType: EntityType, position: Vec3, rotation: EntityRotation): EndCrystal {
            return EndCrystal(connection, entityType, position, rotation)
        }
    }
}
