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

package de.bixilon.minosoft.data.physics.properties

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i

class PositioningProperties(val properties: EntityPhysicsProperties<*>) {
    var rotation = EntityRotation.EMPTY

    var position = Vec3d.EMPTY
    var chunkPosition = Vec2i.EMPTY
    var blockPosition = Vec3i.EMPTY
    var sectionHeight = 0
    var inChunkSectionPosition = Vec3i.EMPTY

    val eyeHeight: Float get() = properties.entity.dimensions.y * 0.85f
    var eyePosition: Vec3 = Vec3.EMPTY
}
