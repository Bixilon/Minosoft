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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.container.processing

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.block.container.processing.BrewingStandBlockEntity
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.horizontal
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

open class BrewingStandBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : ProcessingBlock<BrewingStandBlockEntity>(resourceLocation, registries, data), RandomDisplayTickable {
    private val smokeParticle = registries.particleType[SmokeParticle]

    override fun randomDisplayTick(connection: PlayConnection, state: BlockState, position: BlockPosition, random: Random) {
        smokeParticle?.let {
            connection.world += SmokeParticle(
                connection,
                position.toVec3d + Vec3d(0.4, 0.7, 0.4) + Vec3d.horizontal({ random.nextDouble() * 0.2 }, random.nextDouble() * 0.3),
                Vec3d.EMPTY,
                it.default(),
            )
        }
    }

    companion object : PixLyzerBlockFactory<BrewingStandBlock> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): BrewingStandBlock {
            return BrewingStandBlock(resourceLocation, registries, data)
        }
    }
}

