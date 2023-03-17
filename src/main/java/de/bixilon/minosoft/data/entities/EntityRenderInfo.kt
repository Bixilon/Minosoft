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

package de.bixilon.minosoft.data.entities

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.blockPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class EntityRenderInfo(private val entity: Entity) : Drawable, Tickable {
    private var position0 = Vec3(entity.physics.position)
    private var position1 = position0

    var position: Vec3 = position0
        private set
    var eyePosition: Vec3 = position
        private set
    var cameraAABB: AABB = AABB.EMPTY
        private set
    var eyeBlockPosition = position1.blockPosition
        private set

    private var rotation0 = entity.physics.rotation
    private var rotation1 = rotation0
    var rotation: EntityRotation = rotation0
        private set


    private fun interpolatePosition(delta: Float) {
        position = Vec3Util.interpolateLinear(delta, position0, position1)
        eyePosition = position + Vec3(0.0f, entity.eyeHeight, 0.0f)
        cameraAABB = entity.defaultAABB + position
        eyeBlockPosition = position1.blockPosition
    }

    private fun interpolateRotation(delta: Float) {
        rotation = EntityRotation(interpolateLinear(delta, rotation0.yaw, rotation1.yaw), interpolateLinear(delta, rotation0.pitch, rotation1.pitch))
    }

    override fun draw(millis: Long) {
        val delta = (millis - entity.lastTickTime) / ProtocolDefinition.TICK_TIMEf
        interpolatePosition(delta)
        interpolateRotation(delta)
    }

    override fun tick() {
        position0 = position1
        position1 = Vec3(entity.physics.position)

        rotation0 = rotation1
        rotation1 = entity.physics.rotation
    }
}
