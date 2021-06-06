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

package de.bixilon.minosoft.gui.rendering.input.camera

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i

data class RaycastHit(
    val position: Vec3d,
    val blockPosition: Vec3i,
    val distance: Double,
    val blockState: BlockState,
    val hitDirection: Directions,
    val steps: Int,
) {
    val hitPosition = position.minus(blockPosition)
}
