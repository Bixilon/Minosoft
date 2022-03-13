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

package de.bixilon.minosoft.gui.rendering.entity

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import glm_.vec3.Vec3

class EntityRenderInfo(val entity: Entity) {
    private var previousFrameId = -1L
    var cameraPosition: Vec3 = Vec3.EMPTY
        private set
    var aabb: AABB = AABB.EMPTY
        private set
    var rotation = EntityRotation(0, 0)
        private set
    var eyeHeight = 0.0f
        private set

    fun draw(frameId: Long, time: Long) {
        if (frameId <= previousFrameId) {
            return
        }
        this.previousFrameId = frameId
        this.cameraPosition = entity.physics.positioning.eyePosition
        this.aabb = entity.physics.other.aabb
        this.rotation = entity.physics.positioning.rotation
        this.eyeHeight = entity.physics.positioning.eyeHeight
        // ToDo
    }

    fun reset() {

    }
}
