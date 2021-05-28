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

package de.bixilon.minosoft.data.mappings.blocks.types

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.blocks.BlockUsages
import de.bixilon.minosoft.data.mappings.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.mappings.items.tools.ShovelItem
import de.bixilon.minosoft.data.mappings.versions.Registries
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.gui.rendering.input.camera.RaycastHit
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.CampfireSmokeParticle
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import kotlin.random.Random

open class CampfireBlock(resourceLocation: ResourceLocation, mappings: Registries, data: JsonObject) : Block(resourceLocation, mappings, data) {

    private fun extinguish(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i) {
        val particleRenderer = connection.rendering?.renderWindow?.get(ParticleRenderer) ?: return
        for (i in 0 until 20) {
            spawnSmokeParticles(connection, particleRenderer, blockState, blockPosition, true)
        }
    }

    fun spawnSmokeParticles(connection: PlayConnection, particleRenderer: ParticleRenderer, blockState: BlockState, blockPosition: Vec3i, extinguished: Boolean) {
        val horizontal = { 0.5f + Random.nextFloat() / 3.0f * if (Random.nextBoolean()) 1.0f else -1.0f }
        val position = Vec3(
            blockPosition.x + horizontal(),
            blockPosition.y + Random.nextFloat() + Random.nextFloat(),
            blockPosition.z + horizontal()
        )
        val isSignal = blockState.properties[BlockProperties.CAMPFIRE_SIGNAL_FIRE] == true

        val data = connection.registries.particleTypeRegistry[if (isSignal) {
            CampfireSmokeParticle.SignalSmokeParticleFactory
        } else {
            CampfireSmokeParticle.CosySmokeParticleFactory
        }]!!

        particleRenderer.add(CampfireSmokeParticle(connection, particleRenderer, position, Vec3(0.0f, 0.07f, 0.0f), data.simple(), isSignal))

        if (extinguished) {
            // ToDo: Spawn smoke particles
        }
    }

    override fun onUse(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, raycastHit: RaycastHit, hands: Hands, itemStack: ItemStack?): BlockUsages {
        if (itemStack?.item !is ShovelItem || blockState.properties[BlockProperties.LIT] != true) {
            return super.onUse(connection, blockState, blockPosition, raycastHit, hands, itemStack)
        }
        connection.world.setBlockState(blockPosition, blockState.withProperties(BlockProperties.LIT to false))
        extinguish(connection, blockState, blockPosition)
        return BlockUsages.SUCCESS
    }
}
