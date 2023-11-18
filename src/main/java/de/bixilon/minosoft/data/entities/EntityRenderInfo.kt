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

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.data.entities.EntityRotation.Companion.interpolateYaw
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.addedY
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class EntityRenderInfo(private val entity: Entity) : Drawable, Tickable {
    private var position0 = Vec3d.EMPTY
    private var position1 = Vec3d(entity.physics.position)
    private var defaultAABB = entity.defaultAABB


    private var eyeHeight0 = 0.0f
    private var eyeHeight1 = eyeHeight0

    var position: Vec3d = position1
        private set
    var eyePosition: Vec3d = position
        private set
    var cameraAABB: AABB = AABB.EMPTY
        private set

    private var rotation0 = EntityRotation.EMPTY
    private var rotation1 = entity.physics.rotation
    var rotation: EntityRotation = rotation1
        private set

    init {
        interpolateAABB(true)
    }


    private fun interpolatePosition(delta: Float) {
        val position1 = this.position1
        val eyeHeight1 = this.eyeHeight1

        if (position.isEqual(position1) && eyeHeight0 == eyeHeight1) {
            interpolateAABB(false)
            return
        }

        position = Vec3dUtil.interpolateLinear(delta.toDouble(), position0, position1)

        eyePosition = position.addedY(interpolateLinear(delta, eyeHeight0, eyeHeight1).toDouble())

        interpolateAABB(true)
    }

    private fun interpolateAABB(force: Boolean) {
        val defaultAABB = entity.defaultAABB
        if (!force && this.defaultAABB === defaultAABB) {
            return
        }
        cameraAABB = defaultAABB + position
    }

    private fun interpolateRotation(delta: Float) {
        val rotation1 = this.rotation1
        if (rotation == rotation1) {
            return
        }
        val rotation0 = this.rotation0
        rotation = EntityRotation(interpolateYaw(delta, rotation0.yaw, rotation1.yaw), interpolateLinear(delta, rotation0.pitch, rotation1.pitch))
    }

    override fun draw(millis: Long) {
        val delta = (millis - entity.lastTickTime) / ProtocolDefinition.TICK_TIMEf
        interpolatePosition(delta)
        interpolateRotation(delta)
    }

    private fun tickPosition() {
        val entityPosition = entity.physics.position
        if (position0.isEqual(entityPosition) && position1.isEqual(entityPosition)) return

        position0 = position1
        position1 = entityPosition
    }

    private fun tickRotation() {
        val entityRotation = entity.physics.rotation
        if (rotation0 === entityRotation && rotation1 === entityRotation) return

        rotation0 = rotation1
        rotation1 = entityRotation
    }

    private fun tickEyeHeight() {
        val eyeHeight = entity.eyeHeight
        if (eyeHeight0 == eyeHeight && eyeHeight1 == eyeHeight) return

        eyeHeight0 = eyeHeight1
        eyeHeight1 = eyeHeight
    }

    override fun tick() {
        tickPosition()
        tickEyeHeight()
        tickRotation()
    }

    private fun Vec3d.isEqual(other: Vec3d): Boolean {
        val ta = this.array
        val oa = other.array
        return ta[0] == oa[0] && ta[1] == oa[1] && oa[2] == ta[2]
    }
}
