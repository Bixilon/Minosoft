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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.light.CustomLightProperties
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.fluid.fluids.Fluid
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.AABB
import de.bixilon.minosoft.data.registries.shapes.VoxelShape
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

open class FluidBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : PixLyzerBlock(resourceLocation, registries, data), FluidHolder {
    override val fluid: Fluid = registries.fluid[data["still_fluid"]]!!

    override fun getOutlineShape(connection: PlayConnection, blockState: BlockState): VoxelShape {
        return VoxelShape(mutableListOf(AABB(Vec3.EMPTY, Vec3(1.0f, fluid.getHeight(blockState), 1.0f))))
    }

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        super.randomTick(connection, blockState, blockPosition, random)
        // ToDo
        fluid.randomTick(connection, blockState, blockPosition, random)
    }

    companion object : PixLyzerBlockFactory<FluidBlock> {
        val LIGHT_PROPERTIES = CustomLightProperties(true, false, true)

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): FluidBlock {
            return FluidBlock(resourceLocation, registries, data)
        }
    }
}
