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
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.CampfireSmokeParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.lava.LavaParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.noise
import de.bixilon.minosoft.gui.rendering.util.VecUtil.verticalPlus
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.chance
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import kotlin.random.Random

open class CampfireBlock(resourceLocation: ResourceLocation, registries: Registries, data: JsonObject) : Block(resourceLocation, registries, data) {
    val lavaParticles = data["lava_particles"]?.asBoolean ?: true

    private val campfireCrackleSoundEvent = registries.soundEventRegistry[CAMPFIRE_CRACKLE_SOUND_RESOURCE_LOCATION]!!
    private val cosySmokeParticle = registries.particleTypeRegistry[CampfireSmokeParticle.CosyFactory]!!
    private val signalSmokeParticle = registries.particleTypeRegistry[CampfireSmokeParticle.SignalFactory]!!
    private val lavaParticle = registries.particleTypeRegistry[LavaParticle]!!
    private val smokeParticle = registries.particleTypeRegistry[SmokeParticle]!!

    private fun extinguish(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i) {
        for (i in 0 until 20) {
            spawnSmokeParticles(connection, blockState, blockPosition, true)
        }
    }

    fun spawnSmokeParticles(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, extinguished: Boolean) {
        let {
            val position = Vec3(blockPosition).verticalPlus(
                { 0.5f + 3.0f.noise },
                Random.nextFloat() + Random.nextFloat() + 0.5f // ToDo: This +0.5f is a temporary fix for not making the particle stuck in ourself
            )

            val isSignal = blockState.properties[BlockProperties.CAMPFIRE_SIGNAL_FIRE] == true

            val particleType = if (isSignal) {
                signalSmokeParticle
            } else {
                cosySmokeParticle
            }

            connection.world += CampfireSmokeParticle(connection, position, Vec3(0.0f, 0.07f, 0.0f), particleType.default(), isSignal)
        }

        if (extinguished) {
            val position = Vec3(blockPosition).verticalPlus(
                { 0.5f + 4.0f.noise },
                0.5f
            )
            connection.world += SmokeParticle(connection, position, Vec3(0.0f, 0.005f, 0.0f), smokeParticle.default())
        }
    }

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        if (blockState.properties[BlockProperties.LIT] != true) {
            return
        }
        if (random.chance(10)) {
            connection.world.playSoundEvent(campfireCrackleSoundEvent, blockPosition + Vec3(0.5f), 0.5f + random.nextFloat(), 0.6f + random.nextFloat() * 0.7f)
        }

        if (lavaParticles && random.chance(20)) {
            val position = Vec3(blockPosition) + Vec3(0.5f)
            for (i in 0 until random.nextInt(1) + 1) {
                connection.world += LavaParticle(connection, position, lavaParticle.default())
            }
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

    companion object {
        private val CAMPFIRE_CRACKLE_SOUND_RESOURCE_LOCATION = "minecraft:block.campfire.crackle".asResourceLocation()
    }
}
