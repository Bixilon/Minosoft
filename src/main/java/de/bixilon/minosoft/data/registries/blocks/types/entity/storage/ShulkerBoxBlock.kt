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

package de.bixilon.minosoft.data.registries.blocks.types.entity.storage

import de.bixilon.minosoft.data.colors.DyeColors
import de.bixilon.minosoft.data.entities.block.container.storage.ShulkerBoxBlockEntity
import de.bixilon.minosoft.data.registries.blocks.entites.BlockEntityType
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.DyedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.FullOpaqueBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries

open class ShulkerBoxBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), StorageBlock<ShulkerBoxBlockEntity>, FullOpaqueBlock, BlockWithItem<Item> {
    override val blockEntity: BlockEntityType<ShulkerBoxBlockEntity> = this::blockEntity.inject(this)
    override val item: Item = this::item.inject(identifier)
    override val hardness: Float get() = 2.0f


    companion object : BlockFactory<ShulkerBoxBlock> {
        override val identifier = minecraft("shulker_box")

        override fun build(registries: Registries, settings: BlockSettings) = ShulkerBoxBlock(identifier, settings = settings)
    }

    open class White(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.WHITE

        companion object : BlockFactory<White> {
            override val identifier = minecraft("white_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = White(settings = settings)
        }
    }

    open class Orange(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.ORANGE

        companion object : BlockFactory<Orange> {
            override val identifier = minecraft("orange_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Orange(settings = settings)
        }
    }

    open class Magenta(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.MAGENTA

        companion object : BlockFactory<Magenta> {
            override val identifier = minecraft("magenta_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Magenta(settings = settings)
        }
    }

    open class LightBlue(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.LIGHT_BLUE

        companion object : BlockFactory<LightBlue> {
            override val identifier = minecraft("light_blue_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = LightBlue(settings = settings)
        }
    }

    open class Yellow(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.YELLOW

        companion object : BlockFactory<Yellow> {
            override val identifier = minecraft("yellow_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Yellow(settings = settings)
        }
    }

    open class Lime(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.LIME

        companion object : BlockFactory<Lime> {
            override val identifier = minecraft("lime_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Lime(settings = settings)
        }
    }

    open class Pink(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.PINK

        companion object : BlockFactory<Pink> {
            override val identifier = minecraft("pink_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Pink(settings = settings)
        }
    }

    open class Gray(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.GRAY

        companion object : BlockFactory<Gray> {
            override val identifier = minecraft("gray_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Gray(settings = settings)
        }
    }

    open class LightGray(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.LIGHT_GRAY

        companion object : BlockFactory<LightGray> {
            override val identifier = minecraft("light_gray_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = LightGray(settings = settings)
        }
    }

    open class Cyan(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.CYAN

        companion object : BlockFactory<Cyan> {
            override val identifier = minecraft("cyan_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Cyan(settings = settings)
        }
    }

    open class Purple(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.PURPLE

        companion object : BlockFactory<Purple> {
            override val identifier = minecraft("purple_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Purple(settings = settings)
        }
    }

    open class Blue(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.BLUE

        companion object : BlockFactory<Blue> {
            override val identifier = minecraft("blue_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Blue(settings = settings)
        }
    }

    open class Brown(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.BROWN

        companion object : BlockFactory<Brown> {
            override val identifier = minecraft("brown_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Brown(settings = settings)
        }
    }

    open class Green(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.GREEN

        companion object : BlockFactory<Green> {
            override val identifier = minecraft("green_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Green(settings = settings)
        }
    }

    open class Red(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.RED

        companion object : BlockFactory<Red> {
            override val identifier = minecraft("red_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Red(settings = settings)
        }
    }

    open class Black(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : ShulkerBoxBlock(identifier, settings), DyedBlock {
        override val color: DyeColors get() = DyeColors.BLACK

        companion object : BlockFactory<Black> {
            override val identifier = minecraft("black_shulker_box")

            override fun build(registries: Registries, settings: BlockSettings) = Black(settings = settings)
        }
    }
}
