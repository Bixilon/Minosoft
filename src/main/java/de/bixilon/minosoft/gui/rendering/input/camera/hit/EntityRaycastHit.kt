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

package de.bixilon.minosoft.gui.rendering.input.camera.hit

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.util.KUtil.format
import glm_.vec3.Vec3d

class EntityRaycastHit(
    position: Vec3d,
    distance: Double,
    hitDirection: Directions,
    val entity: Entity,
) : RaycastHit(position, distance, hitDirection) {

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(position.format())
        builder.append(": ")
        builder.append(entity.entityType.resourceLocation)

        builder.append("\n Id: ")
        builder.append(entity.id)

        builder.append("\n UUID: ")
        builder.append(entity.uuid)
        builder.append("\n")

        for ((key, value) in entity.entityMetaDataFormatted) {
            builder.append("\n")
            builder.append(' ')
            builder.append(key)
            builder.append(": ")
            builder.append(value.format())
        }
        return builder.toString()
    }
}
