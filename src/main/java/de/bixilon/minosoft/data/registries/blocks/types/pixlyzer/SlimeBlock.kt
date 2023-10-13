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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.handler.entity.StepHandler
import de.bixilon.minosoft.data.registries.blocks.handler.entity.landing.BouncingHandler
import de.bixilon.minosoft.data.registries.blocks.light.FilteringTransparentProperty
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.hardness.InstantBreakableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.FrictionBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.FullBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.transparency.TranslucentBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.physics.entities.EntityPhysics
import de.bixilon.minosoft.protocol.versions.Version
import kotlin.math.abs

open class SlimeBlock(identifier: ResourceLocation = SlimeBlock.identifier, settings: BlockSettings) : Block(identifier, settings), BouncingHandler, StepHandler, InstantBreakableBlock, FrictionBlock, TranslucentBlock, FullBlock, BlockWithItem<Item>, BlockStateBuilder {
    override val item: Item = this::item.inject(identifier) // TODO
    override val friction: Float get() = 0.8f

    override fun buildState(version: Version, settings: BlockStateSettings) = BlockState(this, settings)

    override fun onEntityStep(entity: Entity, physics: EntityPhysics<*>, position: Vec3i, state: BlockState) {
        val velocity = entity.physics.velocity
        if (abs(velocity.y) >= 0.1) return

        val friction = 0.4 + velocity.y * 0.2
        physics.velocity = Vec3d(velocity.x * friction, velocity.y, velocity.z * friction)
    }

    override fun getLightProperties(blockState: BlockState) = FilteringTransparentProperty

    companion object : BlockFactory<SlimeBlock> {
        override val identifier = minecraft("slime_block")

        override fun build(registries: Registries, settings: BlockSettings): SlimeBlock = SlimeBlock(settings = settings)
    }
}

