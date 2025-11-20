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

package de.bixilon.minosoft.data.registries.blocks.types.building.plants

import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.hardness.InstantBreakableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetTypes
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

abstract class FlowerBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), BlockWithItem<Item>, OutlinedBlock, RandomOffsetBlock, InstantBreakableBlock {
    override val randomOffset get() = RandomOffsetTypes.XZ
    override val item: Item = this::item.inject(identifier)

    override val flags get() = super.flags + BlockStateFlags.MINOR_VISUAL_IMPACT

    override fun getOutlineShape(session: PlaySession, position: BlockPosition, state: BlockState) = SHAPE

    companion object {
        private val SHAPE = AABB(0.3125, 0.0, 0.3125, 0.6875, 0.625, 0.6875)
    }

    open class Dandelion(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<Dandelion> {
            override val identifier = minecraft("dandelion")

            override fun build(registries: Registries, settings: BlockSettings) = Dandelion(settings = settings)
        }
    }

    open class Torchflower(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<Torchflower> {
            override val identifier = minecraft("torchflower")

            override fun build(registries: Registries, settings: BlockSettings) = Torchflower(settings = settings)
        }
    }

    open class Poppy(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<Poppy> {
            override val identifier = minecraft("poppy")

            override fun build(registries: Registries, settings: BlockSettings) = Poppy(settings = settings)
        }
    }

    open class BlueOrchid(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<BlueOrchid> {
            override val identifier = minecraft("blue_orchid")

            override fun build(registries: Registries, settings: BlockSettings) = BlueOrchid(settings = settings)
        }
    }

    open class Allium(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<Allium> {
            override val identifier = minecraft("allium")

            override fun build(registries: Registries, settings: BlockSettings) = Allium(settings = settings)
        }
    }

    open class AzureBluet(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<AzureBluet> {
            override val identifier = minecraft("azure_bluet")

            override fun build(registries: Registries, settings: BlockSettings) = AzureBluet(settings = settings)
        }
    }

    open class RedTulip(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<RedTulip> {
            override val identifier = minecraft("red_tulip")

            override fun build(registries: Registries, settings: BlockSettings) = RedTulip(settings = settings)
        }
    }

    open class OrangeTulip(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<OrangeTulip> {
            override val identifier = minecraft("orange_tulip")

            override fun build(registries: Registries, settings: BlockSettings) = OrangeTulip(settings = settings)
        }
    }

    open class WhiteTulip(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<WhiteTulip> {
            override val identifier = minecraft("white_tulip")

            override fun build(registries: Registries, settings: BlockSettings) = WhiteTulip(settings = settings)
        }
    }

    open class PinkTulip(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<PinkTulip> {
            override val identifier = minecraft("pink_tulip")

            override fun build(registries: Registries, settings: BlockSettings) = PinkTulip(settings = settings)
        }
    }

    open class OxeyeDaisy(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<OxeyeDaisy> {
            override val identifier = minecraft("oxeye_daisy")

            override fun build(registries: Registries, settings: BlockSettings) = OxeyeDaisy(settings = settings)
        }
    }

    open class Cornflower(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<Cornflower> {
            override val identifier = minecraft("cornflower")

            override fun build(registries: Registries, settings: BlockSettings) = Cornflower(settings = settings)
        }
    }

    open class WitherRose(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<WitherRose> {
            override val identifier = minecraft("wither_rose")

            override fun build(registries: Registries, settings: BlockSettings) = WitherRose(settings = settings)
        }
    }

    open class LilyOfTheValley(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : FlowerBlock(identifier, settings) {

        companion object : BlockFactory<LilyOfTheValley> {
            override val identifier = minecraft("lily_of_the_valley")

            override fun build(registries: Registries, settings: BlockSettings) = LilyOfTheValley(settings = settings)
        }
    }
}
