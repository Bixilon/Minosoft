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

package de.bixilon.minosoft.data.registries.blocks.types.light.torch

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.list.MapPropertyList
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.loader.legacy.ModelChooser
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.fire.SmokeParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.slowing.FlameParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.slowing.SoulFireFlameParticle
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.versions.Version
import java.util.*

abstract class TorchBlock(identifier: ResourceLocation, registries: Registries, settings: BlockSettings) : AbstractTorchBlock(identifier, settings), RandomDisplayTickable, ModelChooser {
    protected val smokeParticle = registries.particleType[SmokeParticle]
    protected val flameParticle = registries.particleType[flameParticleType]

    protected abstract val flameParticleType: ParticleFactory<*>?

    override val flags get() = super.flags + BlockStateFlags.MINOR_VISUAL_IMPACT

    override fun bakeModel(context: RenderContext, model: DirectBlockModel) {
        if (context.session.version.flattened || this is WallTorch) return super.bakeModel(context, model)

        this.model = model.choose(mapOf(BlockProperties.FACING to Directions.UP))?.bake()
    }

    private fun spawnSmokeParticles(session: PlaySession, blockPosition: BlockPosition) {
        val particle = session.world.particle ?: return
        val particlePosition = Vec3d(0.5, 0.7, 0.5) + blockPosition
        smokeParticle?.let { particle += SmokeParticle(session, particlePosition, MVec3d.EMPTY) }
        flameParticle?.let { particle += it.factory?.build(session, particlePosition, MVec3d.EMPTY) }
    }

    override fun randomDisplayTick(session: PlaySession, state: BlockState, position: BlockPosition, random: Random) {
        spawnSmokeParticles(session, position)
    }

    abstract class NormalTorchBlock(identifier: ResourceLocation, registries: Registries, settings: BlockSettings) : TorchBlock(identifier, registries, settings) {
        override val flameParticleType get() = FlameParticle
        override val item: Item = this::item.inject(Standing.identifier)
        override fun buildState(version: Version, settings: BlockStateBuilder) = settings.build(this, luminance = 14)

        class Standing(identifier: ResourceLocation = Companion.identifier, registries: Registries, settings: BlockSettings) : NormalTorchBlock(identifier, registries, settings), StandingTorch {

            companion object : BlockFactory<Standing> {
                override val identifier = minecraft("torch")

                override fun build(registries: Registries, settings: BlockSettings) = Standing(registries = registries, settings = settings)
            }
        }

        class Wall(identifier: ResourceLocation = Companion.identifier, registries: Registries, settings: BlockSettings) : NormalTorchBlock(identifier, registries, settings), WallTorch {

            override fun registerProperties(version: Version, list: MapPropertyList) {
                super.registerProperties(version, list)
                list += WallTorch.FACING
            }

            companion object : BlockFactory<Wall> {
                override val identifier = minecraft("wall_torch")

                override fun build(registries: Registries, settings: BlockSettings) = Wall(registries = registries, settings = settings)
            }
        }
    }

    abstract class SoulTorchBlock(identifier: ResourceLocation, registries: Registries, settings: BlockSettings) : TorchBlock(identifier, registries, settings) {
        override val flameParticleType get() = SoulFireFlameParticle
        override val item: Item = this::item.inject(Standing.identifier)
        override fun buildState(version: Version, settings: BlockStateBuilder) = settings.build(this, luminance = 10)

        class Standing(identifier: ResourceLocation = Companion.identifier, registries: Registries, settings: BlockSettings) : SoulTorchBlock(identifier, registries, settings), StandingTorch {

            companion object : BlockFactory<Standing> {
                override val identifier = minecraft("soul_torch")

                override fun build(registries: Registries, settings: BlockSettings) = Standing(registries = registries, settings = settings)
            }
        }

        class Wall(identifier: ResourceLocation = Companion.identifier, registries: Registries, settings: BlockSettings) : SoulTorchBlock(identifier, registries, settings), WallTorch {

            override fun registerProperties(version: Version, list: MapPropertyList) {
                super.registerProperties(version, list)
                list += WallTorch.FACING
            }

            companion object : BlockFactory<Wall> {
                override val identifier = minecraft("soul_wall_torch")

                override fun build(registries: Registries, settings: BlockSettings) = Wall(registries = registries, settings = settings)
            }
        }
    }
}
