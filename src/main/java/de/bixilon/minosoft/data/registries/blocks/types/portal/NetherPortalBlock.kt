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

package de.bixilon.minosoft.data.registries.blocks.types.portal

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.PortalParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import kotlin.random.Random

open class NetherPortalBlock(resourceLocation: ResourceLocation, registries: Registries, data: JsonObject) : Block(resourceLocation, registries, data) {
    private val portalParticleType = registries.particleTypeRegistry[PortalParticle]

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        super.randomTick(connection, blockState, blockPosition, random)

        portalParticleType?.let {
            for (i in 0 until 4) {
                val position = Vec3d(blockPosition) + { random.nextDouble() }
                val velocity = Vec3d.of { (random.nextDouble() - 0.5) * 0.5 }

                val factor = (random.nextInt(2) * 2 + 1).toDouble()

                if (connection.world[blockPosition + Directions.WEST]?.block != this && connection.world[blockPosition + Directions.EAST]?.block != this) {
                    position.x = blockPosition.x + 0.5 + 0.25 * factor
                    velocity.x = random.nextDouble() * 2.0 * factor
                } else {
                    position.z = blockPosition.z + 0.5 + 0.25 * factor
                    velocity.z = random.nextDouble() * 2.0 * factor
                }
                connection.world += PortalParticle(
                    connection,
                    position,
                    velocity,
                    it.default(),
                )
            }
        }
    }
}

