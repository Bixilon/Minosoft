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

package de.bixilon.minosoft.data.registries.fluid.lava

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.fluid.FlowableFluid
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.decide
import glm_.vec3.Vec3i

class LavaFluid(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: JsonObject,
) : FlowableFluid(resourceLocation, registries, data) {
    override val stillTexture: ResourceLocation = "minecraft:block/lava_still".asResourceLocation()
    override val flowingTexture: ResourceLocation = "minecraft:block/lava_flow".asResourceLocation()

    override fun getVelocityMultiplier(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i): Float {
        return (connection.world.dimension?.ultraWarm == true).decide(0.007f, 0.0023333333333333335f)
    }

    override fun matches(other: Fluid): Boolean {
        return other::class.java.isAssignableFrom(LavaFluid::class.java)
    }

}
