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

package de.bixilon.minosoft.data.registries.blocks.types.wall

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.BlockUsages
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.particle.data.DustParticleData
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.data.text.Colors
import de.bixilon.minosoft.gui.rendering.input.camera.hit.RaycastHit
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.dust.DustParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.chance
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import kotlin.random.Random

open class LeverBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : WallMountedBlock(resourceLocation, registries, data) {
    private val dustParticleType = registries.particleTypeRegistry[DustParticle]

    private fun spawnParticles(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, scale: Float) {
        dustParticleType ?: return
        val direction = (blockState.properties[BlockProperties.FACING] as Directions).inverted
        val mountDirection = getRealFacing(blockState)

        val position = (Vec3d(blockPosition) + 0.5).plus((direction.vector * 0.1) + (mountDirection.vector * 0.2))

        connection.world += DustParticle(connection, position, Vec3d.EMPTY, DustParticleData(Colors.TRUE_RED, scale, dustParticleType))
    }

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        if (blockState.properties[BlockProperties.POWERED] != true) {
            return
        }
        if (random.chance(25)) {
            spawnParticles(connection, blockState, blockPosition, 0.5f)
        }
    }

    override fun getPlacementState(connection: PlayConnection, raycastHit: RaycastHit): BlockState? {
        TODO()
    }

    override fun onUse(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, raycastHit: RaycastHit, hands: Hands, itemStack: ItemStack?): BlockUsages {
        val nextState = blockState.cycle(BlockProperties.POWERED)
        connection.world[blockPosition] = nextState
        spawnParticles(connection, nextState, blockPosition, 1.0f)

        return BlockUsages.SUCCESS
    }

    companion object : BlockFactory<LeverBlock> {
        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): LeverBlock {
            return LeverBlock(resourceLocation, registries, data)
        }
    }
}
