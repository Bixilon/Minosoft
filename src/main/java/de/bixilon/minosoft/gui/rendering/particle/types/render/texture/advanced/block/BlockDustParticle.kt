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

package de.bixilon.minosoft.gui.rendering.particle.types.render.texture.advanced.block

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.DefaultBlocks
import de.bixilon.minosoft.data.registries.particle.data.BlockParticleData
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asGray
import de.bixilon.minosoft.gui.rendering.block.renderable.WorldEntryRenderer
import de.bixilon.minosoft.gui.rendering.block.renderable.block.BlockRenderer
import de.bixilon.minosoft.gui.rendering.block.renderable.block.MultipartRenderer
import de.bixilon.minosoft.gui.rendering.block.renderable.fluid.FluidRenderer
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.advanced.AdvancedTextureParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.blockPosition
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2
import glm_.vec3.Vec3d

class BlockDustParticle(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: BlockParticleData) : AdvancedTextureParticle(connection, position, velocity, data) {

    init {
        val blockPosition = position.blockPosition
        var renderer: WorldEntryRenderer? = data.blockState!!.getBlockRenderer(blockPosition)

        if (renderer is MultipartRenderer) {
            renderer = renderer.models.getOrNull(0)
        }

        texture = when (renderer) {
            is BlockRenderer -> renderer.textureMapping.iterator().next().value // ToDo: If this is empty the rendering crashes
            is FluidRenderer -> renderer.stillTexture // ToDo
            else -> TODO()
        }

        gravityStrength = 1.0f
        color = 0.6f.asGray()

        if (data.blockState.block.resourceLocation != DefaultBlocks.GRASS_BLOCK) {
            val tintColor = connection.rendering!!.renderWindow.tintColorCalculator.getTint(connection.world.getBiome(blockPosition), data.blockState, blockPosition)

            tintColor?.let {
                color = RGBColor(color.floatRed * tintColor.floatRed, color.floatGreen * tintColor.floatGreen, color.floatBlue * tintColor.floatBlue)
            }
        }
        scale /= 2.0f

        val randomU = random.nextFloat() * 3.0f
        val randomV = random.nextFloat() * 3.0f

        minUV = Vec2(randomU + 1.0f, randomV) / 4.0f
        maxUV = Vec2(randomU, randomV + 1.0f) / 4.0f
    }


    companion object : ParticleFactory<BlockDustParticle> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:block".toResourceLocation()

        override fun build(connection: PlayConnection, position: Vec3d, velocity: Vec3d, data: ParticleData): BlockDustParticle? {
            check(data is BlockParticleData)
            if (data.blockState == null || data.blockState.block.resourceLocation == DefaultBlocks.MOVING_PISTON) {
                return null
            }
            return BlockDustParticle(connection, position, velocity, data)
        }
    }
}
