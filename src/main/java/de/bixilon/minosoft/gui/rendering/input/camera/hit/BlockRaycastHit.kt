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
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.util.KUtil.format
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i

open class BlockRaycastHit(
    position: Vec3d,
    distance: Double,
    hitDirection: Directions,
    val blockState: BlockState,
    val blockPosition: Vec3i,
) : RaycastHit(position, distance, hitDirection) {
    val hitPosition = position - blockPosition

    override fun toString(): String {
        val ret = StringBuilder()
        ret.append(blockPosition)
        ret.append(": ")
        ret.append(blockState.block.resourceLocation)

        for ((key, value) in blockState.properties) {
            ret.append('\n')
            ret.append(' ')
            ret.append(key.group)
            ret.append(": ")
            ret.append(value.format())
        }

        return ret.toString()
    }
}
