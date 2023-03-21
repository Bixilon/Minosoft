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

package de.bixilon.minosoft.data.registries.blocks.types.stone

import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.legacy.FlatteningRenamedModel
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.FullOpaqueBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.pickaxe.PickaxeRequirement
import de.bixilon.minosoft.data.registries.registries.Registries

abstract class RockBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), PickaxeRequirement, FullOpaqueBlock, BlockStateBuilder, BlockWithItem<Item> {
    override val item: Item = this::item.inject(identifier)
    override val hardness: Float get() = 1.5f

    override fun buildState(settings: BlockStateSettings): BlockState {
        return BlockState(this, settings.luminance)
    }


    open class Stone(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : RockBlock(identifier, settings) {

        companion object : BlockFactory<Stone> {
            override val identifier = minecraft("stone")

            override fun build(registries: Registries, settings: BlockSettings) = Stone(settings = settings)
        }
    }


    open class Granite(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : RockBlock(identifier, settings) {

        companion object : BlockFactory<Granite> {
            override val identifier = minecraft("granite")

            override fun build(registries: Registries, settings: BlockSettings) = Granite(settings = settings)
        }
    }

    open class PolishedGranite(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : RockBlock(identifier, settings), FlatteningRenamedModel {
        override val legacyModelName = minecraft("smooth_granite")

        companion object : BlockFactory<PolishedGranite> {
            override val identifier = minecraft("polished_granite")

            override fun build(registries: Registries, settings: BlockSettings) = PolishedGranite(settings = settings)
        }
    }


    open class Diorite(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : RockBlock(identifier, settings) {

        companion object : BlockFactory<Diorite> {
            override val identifier = minecraft("diorite")

            override fun build(registries: Registries, settings: BlockSettings) = Diorite(settings = settings)
        }
    }

    open class PolishedDiorite(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : RockBlock(identifier, settings), FlatteningRenamedModel {
        override val legacyModelName = minecraft("smooth_diorite")

        companion object : BlockFactory<PolishedDiorite> {
            override val identifier = minecraft("polished_diorite")

            override fun build(registries: Registries, settings: BlockSettings) = PolishedDiorite(settings = settings)
        }
    }


    open class Andesite(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : RockBlock(identifier, settings) {

        companion object : BlockFactory<Andesite> {
            override val identifier = minecraft("andesite")

            override fun build(registries: Registries, settings: BlockSettings) = Andesite(settings = settings)
        }
    }

    open class PolishedAndesite(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : RockBlock(identifier, settings), FlatteningRenamedModel {
        override val legacyModelName = minecraft("smooth_andesite")


        companion object : BlockFactory<PolishedAndesite> {
            override val identifier = minecraft("polished_andesite")

            override fun build(registries: Registries, settings: BlockSettings) = PolishedAndesite(settings = settings)
        }
    }
}
