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

package de.bixilon.minosoft.gui.rendering.entities

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.chunk.models.AABB
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import glm_.vec3.Vec3
import glm_.vec3.Vec3d

class EntityHitBoxMesh(
    val entity: Entity,
) : LineMesh() {
    val aabb = entity.aabb

    init {
        val hitboxColor = when {
            entity.isInvisible -> Minosoft.config.config.game.entities.hitBox.invisibleEntitiesColor
            else -> Minosoft.config.config.game.entities.hitBox.hitBoxColor
        }
        drawAABB(entity.aabb, Vec3d.EMPTY, LINE_WIDTH, hitboxColor)

        val halfWidth = entity.dimensions.x / 2
        val eyeAABB = AABB(Vec3(-halfWidth, entity.eyeHeight - LINE_WIDTH, -halfWidth), Vec3(halfWidth, entity.eyeHeight - LINE_WIDTH, halfWidth))
        drawAABB(eyeAABB, entity.position, LINE_WIDTH, Minosoft.config.config.game.entities.hitBox.eyeHeightColor)
    }


    companion object {
        private const val LINE_WIDTH = 1.0f / 128.0f
    }
}
