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
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.isLit
import de.bixilon.minosoft.data.registries.blocks.properties.list.MapPropertyList
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.particle.data.DustParticleData
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.dust.DustParticle
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.versions.Version
import java.util.*

abstract class RedstoneTorchBlock(identifier: ResourceLocation, registries: Registries, settings: BlockSettings) : AbstractTorchBlock(identifier, settings), RandomDisplayTickable {
    private val redstoneDustParticle = registries.particleType[DustParticle]
    override val item: Item = this::item.inject(Standing.identifier)

    override fun registerProperties(version: Version, list: MapPropertyList) {
        super.registerProperties(version, list)
        list += LIT
    }

    override fun buildState(version: Version, settings: BlockStateBuilder): BlockState {
        val luminance = if (settings.properties[LIT] == true) 7 else 0
        return settings.build(this, luminance = luminance)
    }

    override fun randomDisplayTick(session: PlaySession, state: BlockState, position: BlockPosition, random: Random) {
        val particle = session.world.particle ?: return
        if (!state.isLit()) return

        (redstoneDustParticle)?.let { particle += it.factory?.build(session, Vec3d(position) + Vec3d(0.5, 0.7, 0.5) + (Vec3d(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5) * 0.2), MVec3d.EMPTY, DustParticleData(Colors.TRUE_RED.rgba(), 1.0f, it)) }
    }

    companion object {
        val LIT = BlockProperties.LIT
    }

    class Standing(identifier: ResourceLocation = Companion.identifier, registries: Registries, settings: BlockSettings) : RedstoneTorchBlock(identifier, registries, settings), StandingTorch {

        companion object : BlockFactory<Standing> {
            override val identifier = minecraft("redstone_torch")

            override fun build(registries: Registries, settings: BlockSettings) = Standing(registries = registries, settings = settings)
        }
    }

    class Wall(identifier: ResourceLocation = Companion.identifier, registries: Registries, settings: BlockSettings) : RedstoneTorchBlock(identifier, registries, settings), WallTorch {

        override fun registerProperties(version: Version, list: MapPropertyList) {
            super.registerProperties(version, list)
            list += WallTorch.FACING
        }

        companion object : BlockFactory<Wall> {
            override val identifier = minecraft("redstone_wall_torch")

            override fun build(registries: Registries, settings: BlockSettings) = Wall(registries = registries, settings = settings)
        }
    }
}
