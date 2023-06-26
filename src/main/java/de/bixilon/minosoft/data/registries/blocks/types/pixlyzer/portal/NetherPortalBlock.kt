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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.portal

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.PixLyzerBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.PortalParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

open class NetherPortalBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : PixLyzerBlock(resourceLocation, registries, data), RandomDisplayTickable {
    private val portalParticleType = registries.particleType[PortalParticle]

    override fun randomDisplayTick(connection: PlayConnection, state: BlockState, position: BlockPosition, random: Random) {
        portalParticleType?.let {
            for (i in 0 until 4) {
                val particlePosition = Vec3d(position) + { random.nextDouble() }
                val velocity = Vec3d.of { (random.nextDouble() - 0.5) * 0.5 }

                val factor = (random.nextInt(2) * 2 + 1).toDouble()

                if (connection.world[position + Directions.WEST]?.block != this && connection.world[position + Directions.EAST]?.block != this) {
                    particlePosition.x = position.x + 0.5 + 0.25 * factor
                    velocity.x = random.nextDouble() * 2.0 * factor
                } else {
                    particlePosition.z = position.z + 0.5 + 0.25 * factor
                    velocity.z = random.nextDouble() * 2.0 * factor
                }
                connection.world += PortalParticle(
                    connection,
                    particlePosition,
                    velocity,
                    it.default(),
                )
            }
        }
    }

    companion object : PixLyzerBlockFactory<NetherPortalBlock> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): NetherPortalBlock {
            return NetherPortalBlock(resourceLocation, registries, data)
        }
    }
}

