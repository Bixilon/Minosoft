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
import de.bixilon.minosoft.data.registries.identified.ResourceLocation

@Deprecated("directly in integrated registries")
object DefaultTints {

    fun init(manager: TintManager) {
        manager.applyTo(setOf(MinecraftBlocks.GRASS_BLOCK, MinecraftBlocks.FERN, MinecraftBlocks.GRASS, MinecraftBlocks.POTTED_FERN), manager.grassTintCalculator)
        manager.applyTo(setOf(MinecraftBlocks.LARGE_FERN, MinecraftBlocks.TALL_GRASS), TallGrassTintCalculator(manager.grassTintCalculator))
        manager.applyTo(setOf(MinecraftBlocks.SPRUCE_LEAVES), StaticTintProvider(0x619961))
        manager.applyTo(setOf(MinecraftBlocks.BIRCH_LEAVES), StaticTintProvider(0x80A755))
        manager.applyTo(setOf(MinecraftBlocks.OAK_LEAVES, MinecraftBlocks.JUNGLE_LEAVES, MinecraftBlocks.ACACIA_LEAVES, MinecraftBlocks.DARK_OAK_LEAVES, MinecraftBlocks.VINE), manager.foliageTintCalculator)
        manager.applyTo(setOf(MinecraftBlocks.REDSTONE_WIRE), RedstoneWireTintCalculator)
        manager.applyTo(setOf(MinecraftBlocks.WATER_CAULDRON, MinecraftBlocks.CAULDRON, MinecraftBlocks.WATER), WaterTintProvider)
        manager.applyTo(setOf(MinecraftBlocks.SUGAR_CANE), SugarCaneTintCalculator(manager.grassTintCalculator))
        manager.applyTo(setOf(MinecraftBlocks.ATTACHED_MELON_STEM, MinecraftBlocks.ATTACHED_PUMPKIN_STEM), StaticTintProvider(0xE0C71C))
        manager.applyTo(setOf(MinecraftBlocks.MELON_STEM, MinecraftBlocks.PUMPKIN_STEM), StemTintCalculator)
        manager.applyTo(setOf(MinecraftBlocks.LILY_PAD), StaticTintProvider(block = 0x208030, item = 0x71C35C))
    }

    private fun TintManager.applyTo(names: Set<ResourceLocation>, provider: TintProvider) {
        for (name in names) {
            connection.registries.block[name]?.tintProvider = provider
            connection.registries.item[name]?.tintProvider = provider
        }
    }
}
