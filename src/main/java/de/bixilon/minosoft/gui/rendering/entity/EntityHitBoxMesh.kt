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

package de.bixilon.minosoft.gui.rendering.entity

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import glm_.vec3.Vec3

class EntityHitBoxMesh(
    renderWindow: RenderWindow,
    val entity: Entity,
    val aabb: AABB,
) : LineMesh(renderWindow) {
    var needsUpdate = true
    var visible = false

    init {
        val hitBoxColor = when {
            entity.isInvisible -> Minosoft.config.config.game.entities.hitBox.invisibleEntitiesColor
            else -> Minosoft.config.config.game.entities.hitBox.hitBoxColor
        }
        drawAABB(aabb, RenderConstants.DEFAULT_LINE_WIDTH, hitBoxColor)

        val eyeHeight = aabb.min.y + entity.eyeHeight
        val eyeAABB = AABB(Vec3(aabb.min.x, eyeHeight, aabb.min.z), Vec3(aabb.max.x, eyeHeight, aabb.max.z)).hShrink(RenderConstants.DEFAULT_LINE_WIDTH)
        drawAABB(eyeAABB, RenderConstants.DEFAULT_LINE_WIDTH, Minosoft.config.config.game.entities.hitBox.eyeHeightColor)
    }

}
