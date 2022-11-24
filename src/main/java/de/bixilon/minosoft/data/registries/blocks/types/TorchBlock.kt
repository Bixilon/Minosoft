/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

open class TorchBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : Block(resourceLocation, registries, data) {
    protected val smokeParticle = registries.particleTypeRegistry[SmokeParticle]
    protected val flameParticle = registries.particleTypeRegistry[data["flame_particle"]]


    private fun spawnSmokeParticles(connection: PlayConnection, blockPosition: Vec3i) {
        val particlePosition = Vec3d(0.5, 0.7, 0.5) + blockPosition
        smokeParticle?.let { connection.world += SmokeParticle(connection, Vec3d(particlePosition), Vec3d.EMPTY) }
        flameParticle?.let { connection.world += it.factory?.build(connection, Vec3d(particlePosition), Vec3d.EMPTY) }
    }

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        spawnSmokeParticles(connection, blockPosition)
    }

    companion object : BlockFactory<TorchBlock> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): TorchBlock {
            return TorchBlock(resourceLocation, registries, data)
        }
    }
}
