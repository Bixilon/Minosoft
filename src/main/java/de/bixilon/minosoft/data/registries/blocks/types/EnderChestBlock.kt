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

package de.bixilon.minosoft.data.registries.blocks.types

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.PortalParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import kotlin.random.Random

open class EnderChestBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : Block(resourceLocation, registries, data) {
    private val portalParticle = registries.particleTypeRegistry[PortalParticle]


    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        portalParticle ?: return
        for (i in 0 until 3) {
            val xFactor = random.nextInt(2) * 2 - 1
            val zFactor = random.nextInt(2) * 2 - 1
            val position = blockPosition.toVec3d + Vec3d(
                0.5 + 0.25 * xFactor,
                random.nextDouble(),
                0.5 + 0.25 * zFactor,
            )
            val velocity = Vec3d(
                random.nextDouble() * xFactor,
                (random.nextDouble() - 0.5) * 0.125,
                random.nextDouble() * zFactor,
            )

            connection.world += PortalParticle(connection, position, velocity, portalParticle.default())
        }
    }

    companion object : BlockFactory<EnderChestBlock> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): EnderChestBlock {
            return EnderChestBlock(resourceLocation, registries, data)
        }
    }
}
