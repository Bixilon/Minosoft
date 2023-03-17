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

import de.bixilon.minosoft.data.colors.DyeColors
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.DyedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.CustomDiggingBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.FullOpaqueBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.shears.ShearsItem
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class WoolBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), FullOpaqueBlock, BlockStateBuilder, CustomDiggingBlock, DyedBlock, BlockWithItem<Item> {
    override val item: Item = this::item.inject(identifier)
    override val hardness: Float get() = 0.8f

    override fun buildState(settings: BlockStateSettings): BlockState {
        return BlockState(this, settings.luminance)
    }

    override fun getMiningSpeed(connection: PlayConnection, state: BlockState, stack: ItemStack): Float {
        if (stack.item.item is ShearsItem) {
            return 5.0f
        }
        return 1.0f
    }

    open class WhiteWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.WHITE

        companion object : BlockFactory<WhiteWool> {
            override val identifier = minecraft("white_wool")

            override fun build(registries: Registries, settings: BlockSettings) = WhiteWool(settings = settings)
        }
    }

    open class OrangeWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.ORANGE

        companion object : BlockFactory<OrangeWool> {
            override val identifier = minecraft("orange_wool")

            override fun build(registries: Registries, settings: BlockSettings) = OrangeWool(settings = settings)
        }
    }

    open class MagentaWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.MAGENTA

        companion object : BlockFactory<MagentaWool> {
            override val identifier = minecraft("magenta_wool")

            override fun build(registries: Registries, settings: BlockSettings) = MagentaWool(settings = settings)
        }
    }

    open class LightBlueWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.LIGHT_BLUE

        companion object : BlockFactory<LightBlueWool> {
            override val identifier = minecraft("light_blue_wool")

            override fun build(registries: Registries, settings: BlockSettings) = LightBlueWool(settings = settings)
        }
    }

    open class YellowWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.YELLOW

        companion object : BlockFactory<YellowWool> {
            override val identifier = minecraft("yellow_wool")

            override fun build(registries: Registries, settings: BlockSettings) = YellowWool(settings = settings)
        }
    }

    open class LimeWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.LIME

        companion object : BlockFactory<LimeWool> {
            override val identifier = minecraft("lime_wool")

            override fun build(registries: Registries, settings: BlockSettings) = LimeWool(settings = settings)
        }
    }

    open class PinkWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.PINK

        companion object : BlockFactory<PinkWool> {
            override val identifier = minecraft("pink_wool")

            override fun build(registries: Registries, settings: BlockSettings) = PinkWool(settings = settings)
        }
    }

    open class GrayWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.GRAY

        companion object : BlockFactory<GrayWool> {
            override val identifier = minecraft("gray_wool")

            override fun build(registries: Registries, settings: BlockSettings) = GrayWool(settings = settings)
        }
    }

    open class LightGrayWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.LIGHT_GRAY

        companion object : BlockFactory<LightGrayWool> {
            override val identifier = minecraft("light_gray_wool")

            override fun build(registries: Registries, settings: BlockSettings) = LightGrayWool(settings = settings)
        }
    }

    open class CyanWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.CYAN

        companion object : BlockFactory<CyanWool> {
            override val identifier = minecraft("cyan_wool")

            override fun build(registries: Registries, settings: BlockSettings) = CyanWool(settings = settings)
        }
    }

    open class PurpleWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.PURPLE

        companion object : BlockFactory<PurpleWool> {
            override val identifier = minecraft("purple_wool")

            override fun build(registries: Registries, settings: BlockSettings) = PurpleWool(settings = settings)
        }
    }

    open class BlueWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.BLUE

        companion object : BlockFactory<BlueWool> {
            override val identifier = minecraft("blue_wool")

            override fun build(registries: Registries, settings: BlockSettings) = BlueWool(settings = settings)
        }
    }

    open class BrownWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.BROWN

        companion object : BlockFactory<BrownWool> {
            override val identifier = minecraft("brown_wool")

            override fun build(registries: Registries, settings: BlockSettings) = BrownWool(settings = settings)
        }
    }

    open class GreenWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.GREEN

        companion object : BlockFactory<GreenWool> {
            override val identifier = minecraft("green_wool")

            override fun build(registries: Registries, settings: BlockSettings) = GreenWool(settings = settings)
        }
    }

    open class RedWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.RED

        companion object : BlockFactory<RedWool> {
            override val identifier = minecraft("red_wool")

            override fun build(registries: Registries, settings: BlockSettings) = RedWool(settings = settings)
        }
    }

    open class BlackWool(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.BLACK

        companion object : BlockFactory<BlackWool> {
            override val identifier = minecraft("black_wool")

            override fun build(registries: Registries, settings: BlockSettings) = BlackWool(settings = settings)
        }
    }
}
