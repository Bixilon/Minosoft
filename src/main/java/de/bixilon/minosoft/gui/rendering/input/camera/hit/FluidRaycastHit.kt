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
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.fluid.Fluid
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i

class FluidRaycastHit(
    position: Vec3d,
    distance: Double,
    hitDirection: Directions,
    val blockState: BlockState,
    val blockPosition: Vec3i,
    val fluid: Fluid,
) : RaycastHit(position, distance, hitDirection) {

    override fun toString(): String {
        return "$blockPosition: ${fluid.resourceLocation}\n Height: ${fluid.getHeight(blockState)}\n Level: ${blockState.properties[BlockProperties.FLUID_LEVEL]}"
    }
}
