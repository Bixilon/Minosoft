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
package de.bixilon.minosoft.data.entities.entities.item

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.physics.entities.item.PrimedTNTPhysics
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class PrimedTNT(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val fuseTime: Int
        get() = data.get(FUSE_TIME_DATA, 80)

    override fun tick() {
        if (fuseTime <= 0) return

        super.tick()

        val position = physics.position
        connection.world += SmokeParticle(connection, position + SMOKE_OFFSET, Vec3d.EMPTY)
    }

    override fun createPhysics() = PrimedTNTPhysics(this)

    companion object : EntityFactory<PrimedTNT> {
        private val SMOKE_OFFSET = Vec3d(0.0, 0.5, 0.0)
        override val identifier: ResourceLocation = minecraft("tnt")
        private val FUSE_TIME_DATA = EntityDataField("PRIMED_TNT_FUSE_TIME")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): PrimedTNT {
            return PrimedTNT(connection, entityType, data, position, rotation)
        }
    }
}
