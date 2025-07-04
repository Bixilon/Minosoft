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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.advanced.block

import glm_.vec2.Vec2
import glm_.vec3.Vec3d
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.BlockParticleData
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asGray
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.advanced.AdvancedTextureParticle
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

class BlockDustParticle(session: PlaySession, position: Vec3d, velocity: Vec3d, data: BlockParticleData) : AdvancedTextureParticle(session, position, velocity, data) {

    init {
        val blockPosition = position.blockPosition
        check(data.blockState != null)
        val textureRandom = Random(0L)
        texture = (data.blockState.block.model ?: data.blockState.model)?.getParticleTexture(textureRandom, blockPosition)

        gravityStrength = 1.0f
        color = 0.6f.asGray().rgba()

        session.rendering?.context?.tints?.getParticleTint(data.blockState, blockPosition)?.let {
            color = RGBAColor(color.redf * it.redf, color.greenf * it.greenf, color.bluef * it.bluef)
        }
        scale /= 2.0f

        val randomU = random.nextFloat() * 3.0f
        val randomV = random.nextFloat() * 3.0f

        minUV = Vec2(randomU + 1.0f, randomV) / 4.0f
        maxUV = Vec2(randomU, randomV + 1.0f) / 4.0f
    }


    companion object : ParticleFactory<BlockDustParticle> {
        private const val GRAY = 153 shl 16 or (153 shl 8) or 153
        override val identifier: ResourceLocation = "minecraft:block".toResourceLocation()

        override fun build(session: PlaySession, position: Vec3d, velocity: Vec3d, data: ParticleData): BlockDustParticle? {
            check(data is BlockParticleData)
            if (data.blockState == null || data.blockState.block.identifier == MinecraftBlocks.MOVING_PISTON) {
                return null
            }
            return BlockDustParticle(session, position, velocity, data)
        }
    }
}
