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

    open class White(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.WHITE

        companion object : BlockFactory<White> {
            override val identifier = minecraft("white_wool")

            override fun build(registries: Registries, settings: BlockSettings) = White(settings = settings)
        }
    }

    open class Orange(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.ORANGE

        companion object : BlockFactory<Orange> {
            override val identifier = minecraft("orange_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Orange(settings = settings)
        }
    }

    open class Magenta(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.MAGENTA

        companion object : BlockFactory<Magenta> {
            override val identifier = minecraft("magenta_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Magenta(settings = settings)
        }
    }

    open class LightBlue(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.LIGHT_BLUE

        companion object : BlockFactory<LightBlue> {
            override val identifier = minecraft("light_blue_wool")

            override fun build(registries: Registries, settings: BlockSettings) = LightBlue(settings = settings)
        }
    }

    open class Yellow(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.YELLOW

        companion object : BlockFactory<Yellow> {
            override val identifier = minecraft("yellow_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Yellow(settings = settings)
        }
    }

    open class Lime(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.LIME

        companion object : BlockFactory<Lime> {
            override val identifier = minecraft("lime_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Lime(settings = settings)
        }
    }

    open class Pink(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.PINK

        companion object : BlockFactory<Pink> {
            override val identifier = minecraft("pink_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Pink(settings = settings)
        }
    }

    open class Gray(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.GRAY

        companion object : BlockFactory<Gray> {
            override val identifier = minecraft("gray_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Gray(settings = settings)
        }
    }

    open class LightGray(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.LIGHT_GRAY

        companion object : BlockFactory<LightGray> {
            override val identifier = minecraft("light_gray_wool")

            override fun build(registries: Registries, settings: BlockSettings) = LightGray(settings = settings)
        }
    }

    open class Cyan(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.CYAN

        companion object : BlockFactory<Cyan> {
            override val identifier = minecraft("cyan_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Cyan(settings = settings)
        }
    }

    open class Purple(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.PURPLE

        companion object : BlockFactory<Purple> {
            override val identifier = minecraft("purple_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Purple(settings = settings)
        }
    }

    open class Blue(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.BLUE

        companion object : BlockFactory<Blue> {
            override val identifier = minecraft("blue_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Blue(settings = settings)
        }
    }

    open class Brown(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.BROWN

        companion object : BlockFactory<Brown> {
            override val identifier = minecraft("brown_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Brown(settings = settings)
        }
    }

    open class Green(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.GREEN

        companion object : BlockFactory<Green> {
            override val identifier = minecraft("green_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Green(settings = settings)
        }
    }

    open class Red(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.RED

        companion object : BlockFactory<Red> {
            override val identifier = minecraft("red_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Red(settings = settings)
        }
    }

    open class Black(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoolBlock(identifier, settings) {
        override val color: DyeColors get() = DyeColors.BLACK

        companion object : BlockFactory<Black> {
            override val identifier = minecraft("black_wool")

            override fun build(registries: Registries, settings: BlockSettings) = Black(settings = settings)
        }
    }
}
