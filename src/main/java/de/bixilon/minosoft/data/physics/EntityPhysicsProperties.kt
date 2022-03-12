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

package de.bixilon.minosoft.data.physics

import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.physics.pipeline.PhysisPipeline
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i

class EntityPhysicsProperties<E : Entity>(val entity: E) {
    val pipeline = PhysisPipeline<E>()
    var position = Vec3d.EMPTY
    var rotation = EntityRotation.EMPTY
    var chunkPosition = Vec2i.EMPTY
    var blockPosition = Vec3i.EMPTY
    var sectionHeight = 0
    var inChunkSectionPosition = Vec3i.EMPTY
    val eyeHeight: Float get() = entity.dimensions.y * 0.85f

    var velocity = Vec3d.EMPTY

    var aabb = entity.aabb

    var vehicle: Entity? = null
    var passengers: MutableSet<Entity> = synchronizedSetOf()

    var submergedFluid: Fluid? = null
    var fluids: MutableMap<Fluid, Float> = mutableMapOf()

    var fallDistance = 0.0

    var onGround = false
    var isClimbing = false
    var activelyRiding = false

    var eyePosition: Vec3 = Vec3.EMPTY

    fun tick() {
        pipeline.run(entity)
    }

    fun reset() {
        velocity = Vec3d.EMPTY
        // ToDo
    }
}
