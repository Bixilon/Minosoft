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

package de.bixilon.minosoft.gui.rendering.particle.types.render

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.chunk.light.SectionLight
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class RenderParticle(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData? = null) : Particle(connection, position, velocity, data) {
    protected open var scale: Float = 0.1f * (random.nextFloat() * 0.5f + 0.5f) * 2.0f
    protected var color: RGBColor = ChatColors.WHITE

    open val emittingLight = 0
    var light = retrieveLight()

    override fun forceMove(delta: Vec3d) {
        super.forceMove(delta)
        this.light = retrieveLight()
    }

    private fun retrieveLight(): Int {
        val aabb = aabb + position
        var maxBlockLight = emittingLight
        var maxSkyLight = 0

        val chunkPosition = position.chunkPosition
        val chunk = getChunk() ?: return maxBlockLight

        val offset = Vec2i.EMPTY
        val inChunk = Vec3i()
        for (position in aabb.positions()) {
            offset.x = (position.x shr 4) - chunkPosition.x
            offset.y = (position.z shr 4) - chunkPosition.y

            inChunk.x = position.x and 0x0F
            inChunk.y = position.y
            inChunk.z = position.z and 0x0F

            val light = chunk.neighbours.trace(offset)?.light?.get(inChunk) ?: SectionLight.SKY_LIGHT_MASK
            if (light and SectionLight.BLOCK_LIGHT_MASK > maxBlockLight) {
                maxBlockLight = light and SectionLight.BLOCK_LIGHT_MASK
            }
            if (light and SectionLight.SKY_LIGHT_MASK > maxSkyLight) {
                maxSkyLight = light and SectionLight.SKY_LIGHT_MASK
            }
        }

        return maxBlockLight or maxSkyLight
    }
}
