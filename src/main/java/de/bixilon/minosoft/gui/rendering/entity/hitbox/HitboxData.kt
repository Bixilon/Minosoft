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

package de.bixilon.minosoft.gui.rendering.entity.hitbox

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.text.formatting.color.RGBColor

data class HitboxData(
    val color: RGBColor,
    val velocity: Vec3d?,
    val rotation: EntityRotation,
) {

    companion object {

        fun of(entity: Entity): HitboxData? {
            val color = entity.hitboxColor ?: return null
            var velocity: Vec3d? = entity.physics.velocity
            if (velocity!!.length2() < 0.003) {
                velocity = null
            }
            val rotation = entity.renderInfo.rotation

            return HitboxData(color, velocity, rotation)
        }
    }
}
