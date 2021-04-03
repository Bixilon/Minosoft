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
package de.bixilon.minosoft.data.entities.entities.monster

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.entities.EntityFactory
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.protocol.network.Connection
import glm_.vec3.Vec3

class Skeleton(connection: Connection, entityType: EntityType, position: Vec3, rotation: EntityRotation) : AbstractSkeleton(connection, entityType, position, rotation) {

    val isFreezeConverting: Boolean
        get() = entityMetaData.sets[EntityMetaDataFields.SKELETON_STRAY_FREEZE_CONVERTING]


    companion object : EntityFactory<Skeleton> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("skeleton")

        override fun build(connection: Connection, entityType: EntityType, position: Vec3, rotation: EntityRotation): Skeleton {
            return Skeleton(connection, entityType, position, rotation)
        }
    }
}
