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

package de.bixilon.minosoft.data.registries.fluid.water

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.fluid.FlowableFluid
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.water.UnderwaterParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import glm_.vec3.Vec3i
import kotlin.random.Random

class WaterFluid(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: JsonObject,
) : FlowableFluid(resourceLocation, registries, data) {
    override val stillTexture: ResourceLocation = "minecraft:block/water_still".asResourceLocation()
    override val flowingTexture: ResourceLocation = "minecraft:block/water_flow".asResourceLocation()

    override fun getVelocityMultiplier(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i): Float {
        return VELOCITY_MULTIPLIER
    }

    override fun matches(other: Fluid): Boolean {
        return other::class.java.isAssignableFrom(WaterFluid::class.java)
    }

    override fun matches(other: BlockState?): Boolean {
        other ?: return false
        if (other.properties[BlockProperties.WATERLOGGED] == true) {
            return true
        }
        return super.matches(other)
    }


    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        super.randomTick(connection, blockState, blockPosition, random)

        // ToDo
        connection.world += UnderwaterParticle(connection, blockPosition.toVec3d + { random.nextDouble() })
    }

    companion object {
        private const val VELOCITY_MULTIPLIER = 0.014f
    }
}
