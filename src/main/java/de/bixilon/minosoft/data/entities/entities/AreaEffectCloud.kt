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
package de.bixilon.minosoft.data.entities.entities

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.entities.EntityFactory
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3

class AreaEffectCloud(connection: PlayConnection, entityType: EntityType, position: Vec3, rotation: EntityRotation) : Entity(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Ignore radius")
    val ignoreRadius: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.AREA_EFFECT_CLOUD_IGNORE_RADIUS)

    @get:EntityMetaDataFunction(name = "Radius")
    val radius: Float
        get() = entityMetaData.sets.getFloat(EntityMetaDataFields.AREA_EFFECT_CLOUD_RADIUS)

    @get:EntityMetaDataFunction(name = "Color")
    val color: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.AREA_EFFECT_CLOUD_COLOR)

    // ignore radius???
    @get:EntityMetaDataFunction(name = "Is waiting")
    val isWaiting: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.AREA_EFFECT_CLOUD_WAITING)

    @get:EntityMetaDataFunction(name = "Particle")
    val particle: ParticleData
        get() = entityMetaData.sets.getParticle(EntityMetaDataFields.AREA_EFFECT_CLOUD_PARTICLE)


    companion object : EntityFactory<AreaEffectCloud> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("area_effect_cloud")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3, rotation: EntityRotation): AreaEffectCloud {
            return AreaEffectCloud(connection, entityType, position, rotation)
        }
    }
}
