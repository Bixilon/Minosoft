/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.vec.vec3.d.MVec3d
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

abstract class RenderParticle(session: PlaySession, position: Vec3d, velocity: MVec3d, data: ParticleData? = null) : Particle(session, position, velocity, data) {
    protected open var scale: Float = 0.1f * (random.nextFloat() * 0.5f + 0.5f) * 2.0f
    protected var color: RGBAColor = ChatColors.WHITE

    open val emittingLight = 0
    var light = retrieveLight()

    override fun forceMove(delta: Vec3d) {
        super.forceMove(delta)
        this.light = retrieveLight()
    }

    private fun retrieveLight(): LightLevel {
        val aabb = aabb + position
        var maxLevel = LightLevel.EMPTY.with(emittingLight)

        val chunkPosition = position.blockPosition.chunkPosition
        val chunk = getChunk() ?: return maxLevel

        for (position in aabb.positions()) {
            val next = chunk.neighbours.traceChunk(position.chunkPosition - chunkPosition)

            val light = next?.light?.get(position.inChunkPosition) ?: LightLevel(0, LightLevel.MAX_LEVEL) // No chunk is given, assume there is light (otherwise particle might looks badly dark)
            maxLevel = maxLevel.max(light)
        }

        return maxLevel
    }
}
