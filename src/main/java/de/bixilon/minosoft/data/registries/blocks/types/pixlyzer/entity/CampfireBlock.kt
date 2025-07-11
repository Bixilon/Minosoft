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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity

import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.random.RandomUtil.chance
import de.bixilon.minosoft.data.entities.block.CampfireBlockEntity
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.isLit
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.LitBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.campfire.CampfireSmokeParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.lava.LavaParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.horizontalPlus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.noised
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.invoke
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

open class CampfireBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : PixLyzerBlockWithEntity<CampfireBlockEntity>(resourceLocation, registries, data), LitBlock, RandomDisplayTickable {
    val lavaParticles = data["lava_particles"]?.toBoolean() ?: true

    private val cosySmokeParticle = registries.particleType[CampfireSmokeParticle.CosyFactory]!!
    private val signalSmokeParticle = registries.particleType[CampfireSmokeParticle.SignalFactory]!!
    private val lavaParticle = registries.particleType[LavaParticle]!!
    private val smokeParticle = registries.particleType[SmokeParticle]!!

    private fun extinguish(session: PlaySession, blockState: BlockState, blockPosition: BlockPosition, random: Random) {
        for (i in 0 until 20) {
            spawnSmokeParticles(session, blockState, blockPosition, true, random)
        }
    }

    fun spawnSmokeParticles(session: PlaySession, blockState: BlockState, blockPosition: BlockPosition, extinguished: Boolean, random: Random) {
        val particle = session.world.particle ?: return
        val position = Vec3d(blockPosition).horizontalPlus(
            { 0.5 + 3.0.noised(random) },
            random.nextDouble() + random.nextDouble() + 0.5 // ToDo: This +0.5f is a temporary fix for not making the particle stuck in ourself
        )

        val isSignal = isSignal(blockState)

        val particleType = if (isSignal) signalSmokeParticle else cosySmokeParticle

        particle += CampfireSmokeParticle(session, position, SMOKE_VELOCITY, particleType.default(), isSignal)

        if (extinguished) {
            val position = Vec3d(blockPosition).horizontalPlus(
                { 0.5 + 4.0.noised(random) },
                0.5
            )
            particle += SmokeParticle(session, position, EXTINGUISHED_VELOCITY, smokeParticle.default())
        }
    }

    override fun randomDisplayTick(session: PlaySession, state: BlockState, position: BlockPosition, random: Random) {
        val particle = session.world.particle ?: return
        if (!state.isLit()) {
            return
        }
        if (random.chance(10)) {
            session.world.playSoundEvent(CAMPFIRE_CRACKLE_SOUND, Vec3f(0.5f) + position, 0.5f + random.nextFloat(), 0.6f + random.nextFloat() * 0.7f)
        }

        if (lavaParticles && random.chance(20)) {
            val position = Vec3d(position) + 0.5
            for (i in 0 until random.nextInt(1) + 1) {
                particle += LavaParticle(session, position, lavaParticle.default())
            }
        }
    }

    override fun extinguish(session: PlaySession, position: BlockPosition, state: BlockState): Boolean {
        if (!state.isLit()) return false
        session.world[position] = state.withProperties(BlockProperties.LIT to false)
        extinguish(session, state, position, Random())
        return true
    }

    override fun light(session: PlaySession, position: BlockPosition, state: BlockState): Boolean {
        if (state.isLit()) return false
        session.world[position] = state.withProperties(BlockProperties.LIT to true)
        return true
    }

    fun isSignal(state: BlockState): Boolean {
        if (state !is PropertyBlockState) return false
        return state.properties[BlockProperties.CAMPFIRE_SIGNAL_FIRE]?.toBoolean() ?: return false
    }

    companion object : PixLyzerBlockFactory<CampfireBlock> {
        private val CAMPFIRE_CRACKLE_SOUND = "minecraft:block.campfire.crackle".toResourceLocation()
        const val MAX_ITEMS = 4
        private val SMOKE_VELOCITY = Vec3d(0.0f, 0.07f, 0.0f)
        private val EXTINGUISHED_VELOCITY = Vec3d(0.0f, 0.005f, 0.0f)

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): CampfireBlock {
            return CampfireBlock(resourceLocation, registries, data)
        }
    }
}
