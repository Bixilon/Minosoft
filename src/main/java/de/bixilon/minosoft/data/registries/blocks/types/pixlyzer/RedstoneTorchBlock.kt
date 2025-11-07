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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.isLit
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.DustParticleData
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.dust.DustParticle
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import java.util.*

open class RedstoneTorchBlock(identifier: ResourceLocation, registries: Registries, data: Map<String, Any>) : TorchBlock(identifier, registries, data), RandomDisplayTickable {
    private val redstoneDustParticle = registries.particleType[DustParticle]

    override fun randomDisplayTick(session: PlaySession, state: BlockState, position: BlockPosition, random: Random) {
        val particle = session.world.particle ?: return
        if (!state.isLit()) {
            return
        }

        (flameParticle ?: redstoneDustParticle)?.let { particle += it.factory?.build(session, Vec3d(position) + Vec3d(0.5, 0.7, 0.5) + (Vec3d(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5) * 0.2), MVec3d.EMPTY, DustParticleData(Colors.TRUE_RED.rgba(), 1.0f, it)) }
    }

    companion object : PixLyzerBlockFactory<RedstoneTorchBlock> {

        override fun build(identifier: ResourceLocation, registries: Registries, data: Map<String, Any>): RedstoneTorchBlock {
            return RedstoneTorchBlock(identifier, registries, data)
        }
    }
}
