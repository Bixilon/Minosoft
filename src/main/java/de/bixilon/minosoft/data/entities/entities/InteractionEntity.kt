/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class InteractionEntity(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Entity(session, entityType, data, position, rotation) {

    override var defaultAABB: AABB? = null

    @get:SynchronizedEntityData
    val width: Float by data(WIDTH, 1.0f) // TODO: default

    @get:SynchronizedEntityData
    val height: Float by data(HEIGHT, 1.0f) // TODO: default

    private fun updateFlags(force: Boolean) {
        val dimensions = Vec2f(width, height)
        if (!force && this.dimensions == dimensions) return
        this.defaultAABB = createDefaultAABB()
    }


    override fun init() {
        super.init()

        updateFlags(true)
        this::width.observe(this) { updateFlags(false) }
        this::height.observe(this) { updateFlags(false) }
    }

    companion object : EntityFactory<InteractionEntity> {
        override val identifier = minecraft("interaction")
        private val WIDTH = EntityDataField("WIDTH")
        private val HEIGHT = EntityDataField("HEIGHT")
        private val RESPONSE = EntityDataField("RESPONSE")

        override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): InteractionEntity {
            return InteractionEntity(session, entityType, data, position, rotation)
        }
    }
}
