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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.wall

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.random.RandomUtil.chance
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.Companion.getFacing
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.Companion.isPowered
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.InteractBlockHandler
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.DustParticleData
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.dust.DustParticle
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

open class LeverBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : WallMountedBlock(resourceLocation, registries, data), InteractBlockHandler {
    private val dustParticleType = registries.particleType[DustParticle]

    private fun spawnParticles(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, scale: Float) {
        dustParticleType ?: return
        val direction = blockState.getFacing().inverted
        val mountDirection = getRealFacing(blockState)

        val position = (Vec3d(blockPosition) + 0.5).plus((direction.vector * 0.1) + (mountDirection.vector * 0.2))

        connection.world += DustParticle(connection, position, Vec3d.EMPTY, DustParticleData(Colors.TRUE_RED, scale, dustParticleType))
    }

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        if (!blockState.isPowered()) {
            return
        }
        if (random.chance(25)) {
            spawnParticles(connection, blockState, blockPosition, 0.5f)
        }
    }

    override fun getPlacementState(connection: PlayConnection, target: BlockTarget): BlockState? {
        TODO()
    }

    override fun interact(connection: PlayConnection, target: BlockTarget, hand: Hands, stack: ItemStack?): InteractionResults {
        val nextState = target.state.cycle(BlockProperties.POWERED)
        connection.world[target.blockPosition] = nextState
        spawnParticles(connection, nextState, target.blockPosition, 1.0f)

        return InteractionResults.SUCCESS
    }

    companion object : PixLyzerBlockFactory<LeverBlock> {
        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): LeverBlock {
            return LeverBlock(resourceLocation, registries, data)
        }
    }
}
