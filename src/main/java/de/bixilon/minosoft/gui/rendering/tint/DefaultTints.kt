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

package de.bixilon.minosoft.gui.rendering.tint

import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.types.legacy.LegacyBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.PixLyzerBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.pixlyzer.PixLyzerItem
import de.bixilon.minosoft.gui.rendering.tint.tints.StaticTintProvider
import de.bixilon.minosoft.gui.rendering.tint.tints.fluid.WaterTintProvider
import de.bixilon.minosoft.gui.rendering.tint.tints.plants.StemTintCalculator
import de.bixilon.minosoft.gui.rendering.tint.tints.plants.SugarCaneTintCalculator
import de.bixilon.minosoft.gui.rendering.tint.tints.redstone.RedstoneWireTintCalculator

@Deprecated("directly in integrated registries")
object DefaultTints {

    fun init(manager: TintManager) {
        manager.applyTo(setOf(MinecraftBlocks.POTTED_FERN), manager.grass)
        manager.applyTo(setOf(MinecraftBlocks.VINE), manager.foliage) // TODO: inventory color #48b518
        manager.applyTo(setOf(MinecraftBlocks.REDSTONE_WIRE), RedstoneWireTintCalculator)
        manager.applyTo(setOf(MinecraftBlocks.WATER_CAULDRON, MinecraftBlocks.CAULDRON), WaterTintProvider)
        manager.applyTo(setOf(MinecraftBlocks.SUGAR_CANE), SugarCaneTintCalculator(manager.grass))
        manager.applyTo(setOf(MinecraftBlocks.ATTACHED_MELON_STEM, MinecraftBlocks.ATTACHED_PUMPKIN_STEM), StaticTintProvider(0xE0C71C))
        manager.applyTo(setOf(MinecraftBlocks.MELON_STEM, MinecraftBlocks.PUMPKIN_STEM), StemTintCalculator)
        manager.applyTo(setOf(MinecraftBlocks.LILY_PAD), StaticTintProvider(block = 0x208030, item = 0x71C35C))
    }

    private fun TintManager.applyTo(names: Set<ResourceLocation>, provider: TintProvider) {
        for (name in names) {
            when (val block = connection.registries.block[name]) {
                is LegacyBlock -> block.tintProvider = provider
                is PixLyzerBlock -> block.tintProvider = provider
                null -> Unit
                else -> throw IllegalArgumentException("$name should set its tint itself!")
            }
            when (val item = connection.registries.item[name]) {
                is PixLyzerItem -> item.tintProvider = provider
                null -> Unit
                else -> throw IllegalArgumentException("$name should set its tint itself!")
            }
        }
    }
}
