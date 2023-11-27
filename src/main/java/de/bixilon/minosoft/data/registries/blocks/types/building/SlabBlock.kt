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

package de.bixilon.minosoft.data.registries.blocks.types.building

import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.registries.blocks.light.DirectedProperty
import de.bixilon.minosoft.data.registries.blocks.light.OpaqueProperty
import de.bixilon.minosoft.data.registries.blocks.properties.EnumProperty
import de.bixilon.minosoft.data.registries.blocks.properties.Halves
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.AdvancedBlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.WaterloggableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.axe.AxeRequirement
import de.bixilon.minosoft.data.registries.item.items.tool.pickaxe.PickaxeRequirement
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape
import de.bixilon.minosoft.data.registries.shapes.voxel.VoxelShape
import de.bixilon.minosoft.protocol.versions.Version

abstract class SlabBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), BlockStateBuilder, OutlinedBlock, CollidableBlock, BlockWithItem<Item>, WaterloggableBlock {
    override val item: Item = this::item.inject(identifier)
    override val hardness get() = 2.0f

    override fun buildState(version: Version, settings: BlockStateSettings): BlockState {
        val half = settings.properties?.get(HALF) ?: throw IllegalArgumentException("Half not set!")
        val shape = when (half) {
            Halves.LOWER -> BOTTOM_SHAPE
            Halves.UPPER -> TOP_SHAPE
            Halves.DOUBLE -> AbstractVoxelShape.FULL
            else -> Broken()
        }
        val light = if (half == Halves.DOUBLE) OpaqueProperty else DirectedProperty.of(shape)
        return AdvancedBlockState(this, settings.properties, settings.luminance, shape, shape, light)
    }

    companion object {
        private val BOTTOM_SHAPE = VoxelShape(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)
        private val TOP_SHAPE = VoxelShape(0.0, 0.5, 0.0, 1.0, 1.0, 1.0)
        val HALF = EnumProperty("type", Halves)
    }

    abstract class WoodSlab(identifier: ResourceLocation, settings: BlockSettings) : SlabBlock(identifier, settings), AxeRequirement
    abstract class AbstractStoneSlab(identifier: ResourceLocation, settings: BlockSettings) : SlabBlock(identifier, settings), PickaxeRequirement


    /*
    class PetrifiedOak(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<Stone> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Stone(settings = settings)
        }
    }

    class Blackstone(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<Stone> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Stone(settings = settings)
        }
    }

    class PolishedBlackstoneBrick(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<Stone> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Stone(settings = settings)
        }
    }

    class PolishedBlackstone(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<Stone> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Stone(settings = settings)
        }
    }

    class Tuff(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<Stone> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Stone(settings = settings)
        }
    }

    class PolishedTuff(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<Stone> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Stone(settings = settings)
        }
    }

    class TuffBrick(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<Stone> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Stone(settings = settings)
        }
    }

    class CobbleDeepslate(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<Stone> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Stone(settings = settings)
        }
    }

    class PolishedDeepslate(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<Stone> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Stone(settings = settings)
        }
    }

    class DeepslateTile(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<Stone> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Stone(settings = settings)
        }
    }

    class DeepslateBrick(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<Stone> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = Stone(settings = settings)
        }
    }

     */
}
