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

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.plus
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import java.util.*

open class TorchBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : PixLyzerBlock(resourceLocation, registries, data), RandomDisplayTickable {
    protected val smokeParticle = registries.particleType[SmokeParticle]
    protected val flameParticle = registries.particleType[data["flame_particle"]]


    private fun spawnSmokeParticles(session: PlaySession, blockPosition: BlockPosition) {
        val particle = session.world.particle ?: return
        val particlePosition = Vec3d(0.5, 0.7, 0.5) + blockPosition
        smokeParticle?.let { particle += SmokeParticle(session, Vec3d(particlePosition), Vec3d.EMPTY) }
        flameParticle?.let { particle += it.factory?.build(session, Vec3d(particlePosition), Vec3d.EMPTY) }
    }

    override fun randomDisplayTick(session: PlaySession, state: BlockState, position: BlockPosition, random: Random) {
        spawnSmokeParticles(session, position)
    }

    companion object : PixLyzerBlockFactory<TorchBlock> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): TorchBlock {
            return TorchBlock(resourceLocation, registries, data)
        }
    }
}
