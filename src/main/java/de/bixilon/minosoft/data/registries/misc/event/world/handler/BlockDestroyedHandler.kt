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

package de.bixilon.minosoft.data.registries.misc.event.world.handler

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.misc.event.world.WorldEventHandler
import de.bixilon.minosoft.data.registries.particle.data.BlockParticleData
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.advanced.block.BlockDustParticle
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.ceil
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.min
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.max
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object BlockDestroyedHandler : WorldEventHandler {
    override val identifier: ResourceLocation = "minecraft:block_destroyed".toResourceLocation()

    override fun handle(session: PlaySession, position: BlockPosition, data: Int, isGlobal: Boolean) {
        val state = session.registries.blockState.getOrNull(data) ?: return
        handleDestroy(session, position, state)
    }

    fun handleDestroy(session: PlaySession, position: BlockPosition, state: BlockState) {
        state.block.soundGroup?.let { group ->
            group.destroy?.let { session.world.playSoundEvent(it, position, group.volume, group.pitch) }
        }

        addBlockBreakParticles(session, position, state)
    }

    private fun addBlockBreakParticles(session: PlaySession, position: BlockPosition, state: BlockState) {
        val particleRenderer = session.world.particle ?: return
        val type = session.registries.particleType[BlockDustParticle] ?: return
        if (state.block !is OutlinedBlock) return
        val shape = state.block.getOutlineShape(session, position, state) ?: return
        val particleData = BlockParticleData(state, type)

        for (aabb in shape) {
            val delta = (aabb.max - aabb.min).min(1.0)
            val max = (delta * 4.0).ceil().max(2)

            for (x in 0 until max.x) {
                for (y in 0 until max.y) {
                    for (z in 0 until max.z) {
                        val particlePosition = MVec3d(x, y, z).apply { this += 0.5; this /= max }
                        particlePosition *= delta
                        particlePosition += aabb.min

                        particleRenderer += BlockDustParticle(session, particlePosition.unsafe + position, particlePosition - 0.5, particleData)
                    }
                }
            }
        }
    }
}
