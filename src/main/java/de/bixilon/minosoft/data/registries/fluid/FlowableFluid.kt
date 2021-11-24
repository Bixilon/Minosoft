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

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i

abstract class FlowableFluid(
    override val resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : Fluid(resourceLocation, registries, data) {
    abstract val flowingTextureName: ResourceLocation
    var flowingTexture: AbstractTexture? = null


    abstract fun getVelocityMultiplier(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i): Double

    open fun getVelocity(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i): Vec3d {
        if (!this.matches(blockState)) {
            return Vec3d.EMPTY
        }
        val fluidHeight = getHeight(blockState)

        val velocity = Vec3d.EMPTY

        for (direction in Directions.SIDES) {
            val neighbourBlockState = connection.world[blockPosition + direction] ?: continue
            if (!this.matches(neighbourBlockState)) {
                continue
            }
            val height = getHeight(neighbourBlockState)

            var heightDifference = 0.0f

            if (height == 0.0f) {
                // ToDo
            } else {
                heightDifference = fluidHeight - height
            }

            if (heightDifference != 0.0f) {
                velocity += (direction.vectord * heightDifference)
            }
        }

        // ToDo: Falling fluid

        if (velocity.x == 0.0 && velocity.y == 0.0 && velocity.z == 0.0) {
            return velocity
        }

        return velocity.normalize()
    }
}
