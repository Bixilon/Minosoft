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
package de.bixilon.minosoft.data.registries.fluid

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i

abstract class FlowableFluid(
    override val resourceLocation: ResourceLocation,
    registries: Registries,
    data: JsonObject,
) : Fluid(resourceLocation, registries, data) {
    open val flowingTexture: ResourceLocation? = null


    abstract fun getVelocityMultiplier(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i): Double

    open fun getVelocity(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i): Vec3d {
        if (blockState.block !is FluidBlock) {
            return Vec3d.EMPTY
        }
        val thisFluidHeight = blockState.block.fluid.getHeight(blockState)

        val velocity = Vec3d.EMPTY


        for (direction in Directions.SIDES) {
            val neighbourBlockState = connection.world[blockPosition + direction] ?: continue
            if (neighbourBlockState.block !is FluidBlock) {
                continue
            }
            val fluid = neighbourBlockState.block.fluid
            if (!matches(fluid)) {
                continue
            }
            val height = neighbourBlockState.block.fluid.getHeight(neighbourBlockState)

            var magic = 0.0f

            if (height == 0.0f) {
                // ToDo
            } else {
                magic = thisFluidHeight - height
            }

            if (magic != 0.0f) {
                velocity += (direction.vectord * magic)
            }

        }

        // ToDo: Falling fluid

        return velocity.normalize()
    }
}
