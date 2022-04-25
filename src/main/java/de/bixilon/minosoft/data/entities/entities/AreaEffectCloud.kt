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
package de.bixilon.minosoft.data.entities.entities

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class AreaEffectCloud(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, data, position, rotation) {

    override val dimensions: Vec2
        get() = Vec2(radius * 2, super.dimensions.y)

    @get:SynchronizedEntityData(name = "Ignore radius")
    val ignoreRadius: Boolean
        get() = data.getBoolean(IGNORE_RADIUS_DATA, false)

    @get:SynchronizedEntityData(name = "Radius")
    val radius: Float
        get() = data.get(RADIUS_DATA, 0.5f)

    @get:SynchronizedEntityData(name = "Color")
    val color: Int
        get() = data.get(COLOR_DATA, 0)

    // ignore radius???
    @get:SynchronizedEntityData(name = "Is waiting")
    val isWaiting: Boolean
        get() = data.getBoolean(WAITING_DATA, false)

    @get:SynchronizedEntityData(name = "Particle")
    val particle: ParticleData?
        get() = data.get(PARTICLE_DATA, null)


    companion object : EntityFactory<AreaEffectCloud> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("area_effect_cloud")
        private val IGNORE_RADIUS_DATA = EntityDataField("AREA_EFFECT_CLOUD_IGNORE_RADIUS")
        private val RADIUS_DATA = EntityDataField("AREA_EFFECT_CLOUD_RADIUS")
        private val COLOR_DATA = EntityDataField("AREA_EFFECT_CLOUD_COLOR")
        private val WAITING_DATA = EntityDataField("AREA_EFFECT_CLOUD_WAITING")
        private val PARTICLE_DATA = EntityDataField("AREA_EFFECT_CLOUD_PARTICLE")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): AreaEffectCloud {
            return AreaEffectCloud(connection, entityType, data, position, rotation)
        }
    }
}
