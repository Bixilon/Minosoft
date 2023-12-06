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

package de.bixilon.minosoft.data.registries.blocks.types.building.plants

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.properties.EnumProperty
import de.bixilon.minosoft.data.registries.blocks.properties.Halves
import de.bixilon.minosoft.data.registries.blocks.properties.list.MapPropertyList
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.legacy.FlatteningRenamedModel
import de.bixilon.minosoft.data.registries.blocks.types.properties.ReplaceableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.hardness.InstantBreakableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetTypes
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.FullOutlinedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.size.DoubleSizeBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.shears.ShearsRequirement
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.models.block.state.render.PickedBlockRender
import de.bixilon.minosoft.gui.rendering.models.loader.legacy.ModelChooser
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.gui.rendering.tint.TintedBlock
import de.bixilon.minosoft.gui.rendering.tint.tints.grass.TallGrassTintCalculator
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.versions.Version

abstract class DoublePlant(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), ShearsRequirement, BlockWithItem<Item>, FullOutlinedBlock, RandomOffsetBlock, InstantBreakableBlock, ModelChooser, DoubleSizeBlock, ReplaceableBlock {
    override val randomOffset get() = RandomOffsetTypes.XYZ
    override val item: Item = this::item.inject(identifier)

    override fun register(version: Version, list: MapPropertyList) {
        super<Block>.register(version, list)
        list += HALF
    }

    override fun isTop(state: BlockState, connection: PlayConnection): Boolean {
        if (connection.version.flattened) return super.isTop(state, connection)
        return state.block is UpperBlock
    }

    override fun getTop(state: BlockState, connection: PlayConnection): BlockState {
        if (connection.version.flattened) return super.getTop(state, connection)
        return connection.registries.block[UpperBlock]!!.states.default
    }

    override fun getBottom(state: BlockState, connection: PlayConnection): BlockState {
        if (connection.version.flattened) return super.getTop(state, connection)
        Broken("Not enough information to get bottom!")
    }

    override fun bakeModel(context: RenderContext, model: DirectBlockModel) {
        if (context.connection.version.flattened) return super.bakeModel(context, model)

        val top = model.choose(mapOf(HALF to Halves.UPPER))?.bake()
        val bottom = model.choose(mapOf(HALF to Halves.LOWER))?.bake()

        this.model = DoublePlantRenderer(top, bottom)
    }

    class DoublePlantRenderer(
        val top: BlockRender?,
        val bottom: BlockRender?,
    ) : PickedBlockRender {
        override val default: BlockRender? get() = top

        override fun pick(state: BlockState, neighbours: Array<BlockState?>): BlockRender? {
            return if (state.block is UpperBlock) return top else bottom
        }
    }

    companion object {
        val HALF = EnumProperty("half", Halves, Halves.set(Halves.UPPER, Halves.LOWER))
    }

    open class Sunflower(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : DoublePlant(identifier, settings) {

        companion object : BlockFactory<Sunflower> {
            override val identifier = minecraft("sunflower")

            override fun build(registries: Registries, settings: BlockSettings) = Sunflower(settings = settings)
        }
    }

    open class Lilac(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : DoublePlant(identifier, settings), FlatteningRenamedModel {
        override val legacyModelName get() = minecraft("syringa")

        companion object : BlockFactory<Lilac> {
            override val identifier = minecraft("lilac")

            override fun build(registries: Registries, settings: BlockSettings) = Lilac(settings = settings)
        }
    }

    open class TallGrass(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : DoublePlant(identifier, settings), TintedBlock {
        override val tintProvider: TintProvider? = null

        override fun initTint(manager: TintManager) {
            this::tintProvider.forceSet(TallGrassTintCalculator(manager.grass))
        }

        companion object : BlockFactory<TallGrass> {
            override val identifier = minecraft("tall_grass")

            override fun build(registries: Registries, settings: BlockSettings) = TallGrass(settings = settings)
        }
    }

    open class LargeFern(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : DoublePlant(identifier, settings), FlatteningRenamedModel, TintedBlock {
        override val legacyModelName get() = minecraft("double_fern")
        override val tintProvider: TintProvider? = null

        override fun initTint(manager: TintManager) {
            this::tintProvider.forceSet(TallGrassTintCalculator(manager.grass))
        }

        companion object : BlockFactory<LargeFern> {
            override val identifier = minecraft("large_fern")

            override fun build(registries: Registries, settings: BlockSettings) = LargeFern(settings = settings)
        }
    }

    open class RoseBush(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : DoublePlant(identifier, settings), FlatteningRenamedModel {
        override val legacyModelName get() = minecraft("double_rose")

        companion object : BlockFactory<RoseBush> {
            override val identifier = minecraft("rose_bush")

            override fun build(registries: Registries, settings: BlockSettings) = RoseBush(settings = settings)
        }
    }

    open class Peony(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : DoublePlant(identifier, settings), FlatteningRenamedModel {
        override val legacyModelName get() = minecraft("paeonia")

        companion object : BlockFactory<Peony> {
            override val identifier = minecraft("peony")

            override fun build(registries: Registries, settings: BlockSettings) = Peony(settings = settings)
        }
    }

    class UpperBlock(settings: BlockSettings) : DoublePlant(Companion.identifier, settings), TintedBlock {
        override val tintProvider: TintProvider? = null

        init {
            model = Model()
        }

        override fun initTint(manager: TintManager) {
            this::tintProvider.forceSet(TallGrassTintCalculator(manager.grass)) // TODO: only tint if lower block is tinted
        }

        private class Model : PickedBlockRender {
            override val default get() = null // TODO

            override fun pick(state: BlockState, neighbours: Array<BlockState?>): BlockRender? {
                val below = neighbours[Directions.O_DOWN] ?: return null
                return below.model?.nullCast<DoublePlantRenderer>() ?: below.block.model?.nullCast<DoublePlantRenderer>()
            }
        }

        companion object : BlockFactory<UpperBlock> {
            override val identifier = minecraft("double_plant_upper")

            override fun build(registries: Registries, settings: BlockSettings) = UpperBlock(settings = settings)
        }
    }
}
