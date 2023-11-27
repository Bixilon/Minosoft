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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.registries.blocks.light.DirectedProperty
import de.bixilon.minosoft.data.registries.blocks.light.OpaqueProperty
import de.bixilon.minosoft.data.registries.blocks.properties.EnumProperty
import de.bixilon.minosoft.data.registries.blocks.properties.Halves
import de.bixilon.minosoft.data.registries.blocks.properties.list.MapPropertyList
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.AdvancedBlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
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
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.block.BlockModelPrototype
import de.bixilon.minosoft.gui.rendering.models.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.BlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.variant.BlockVariant
import de.bixilon.minosoft.gui.rendering.models.block.state.variant.PropertyVariantBlockModel
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader.Companion.blockState
import de.bixilon.minosoft.gui.rendering.models.loader.legacy.CustomModel
import de.bixilon.minosoft.gui.rendering.models.loader.legacy.ModelChooser
import de.bixilon.minosoft.protocol.versions.Version

abstract class SlabBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), BlockStateBuilder, OutlinedBlock, CollidableBlock, BlockWithItem<Item>, WaterloggableBlock, CustomModel, ModelChooser {
    override val item: Item = this::item.inject(identifier)

    override fun register(version: Version, list: MapPropertyList) {
        list += TYPE
        if (!version.flattened) {
            list += HALF
        }
    }

    override fun loadModel(loader: BlockLoader, version: Version): BlockModelPrototype? {
        if (version.flattened) return super.loadModel(loader, version)
        val doubleFile = ResourceLocation(identifier.namespace, identifier.path.removeSuffix("_slab") + "_double_slab")

        val double = loader.loadState(this, doubleFile.blockState()) ?: return null
        val single = super.loadModel(loader, version) ?: return null

        return StonePrototype(single.model, double.model)
    }

    override fun bakeModel(context: RenderContext, model: DirectBlockModel) {
        if (context.connection.version.flattened) return super.bakeModel(context, model)

        // type was renamed to half
        for (state in states) {
            val properties = if (state is PropertyBlockState) state.properties else emptyMap()
            val patched = properties.toMutableMap()
            patched.remove(TYPE)?.let { patched.put(HALF, it) }

            val apply = model.choose(patched) ?: continue
            state.model = apply.bake()
        }
    }

    override fun buildState(version: Version, settings: BlockStateSettings): BlockState {
        val half = settings.properties?.get(TYPE) ?: throw IllegalArgumentException("Half not set!")
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
        val TYPE = EnumProperty("type", Halves)
        val HALF = EnumProperty("half", Halves) // <1.13
    }

    private class StonePrototype(single: DirectBlockModel, val double: DirectBlockModel) : BlockModelPrototype(single) {

        override fun bake(context: RenderContext, block: Block) {
            if (block !is SlabBlock) return

            block.bakeModel(context, model)
            if (double is PropertyVariantBlockModel) {
                // butt ugly hack
                if (double.variants.size != 1) return
                val map = double.variants.unsafeCast<MutableMap<BlockVariant, BlockStateApply>>()
                map.remove(emptyMap())?.let { map[mapOf(HALF to Halves.DOUBLE)] = it }
            }
            block.bakeModel(context, double)
        }
    }

    abstract class WoodSlab(identifier: ResourceLocation, settings: BlockSettings) : SlabBlock(identifier, settings), AxeRequirement {
        override val hardness get() = 2.0f
    }

    abstract class AbstractStoneSlab(identifier: ResourceLocation, settings: BlockSettings) : SlabBlock(identifier, settings), PickaxeRequirement


    /*
    class PetrifiedOak(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<StoneBlock> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = StoneBlock(settings = settings)
        }
    }

    class Blackstone(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<StoneBlock> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = StoneBlock(settings = settings)
        }
    }

    class PolishedBlackstoneBrick(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<StoneBlock> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = StoneBlock(settings = settings)
        }
    }

    class PolishedBlackstone(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<StoneBlock> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = StoneBlock(settings = settings)
        }
    }

    class Tuff(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<StoneBlock> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = StoneBlock(settings = settings)
        }
    }

    class PolishedTuff(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<StoneBlock> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = StoneBlock(settings = settings)
        }
    }

    class TuffBrick(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<StoneBlock> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = StoneBlock(settings = settings)
        }
    }

    class CobbleDeepslate(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<StoneBlock> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = StoneBlock(settings = settings)
        }
    }

    class PolishedDeepslate(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<StoneBlock> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = StoneBlock(settings = settings)
        }
    }

    class DeepslateTile(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<StoneBlock> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = StoneBlock(settings = settings)
        }
    }

    class DeepslateBrick(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AbstractStoneSlab(identifier, settings) {

        companion object : BlockFactory<StoneBlock> {
            override val identifier = minecraft("stone_slab")

            override fun build(registries: Registries, settings: BlockSettings) = StoneBlock(settings = settings)
        }
    }

     */
}
