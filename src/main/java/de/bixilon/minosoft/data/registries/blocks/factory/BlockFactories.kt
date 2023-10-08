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

@file:Suppress("DEPRECATION")

package de.bixilon.minosoft.data.registries.blocks.factory

import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.air.AirBlock
import de.bixilon.minosoft.data.registries.blocks.types.bee.HoneyBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.WoolBlock
import de.bixilon.minosoft.data.registries.blocks.types.climbing.ScaffoldingBlock
import de.bixilon.minosoft.data.registries.blocks.types.dirt.GrassBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.LavaFluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.BubbleColumnBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.WaterFluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.SlimeBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.snow.PowderSnowBlock
import de.bixilon.minosoft.data.registries.blocks.types.pvp.CobwebBlock
import de.bixilon.minosoft.data.registries.blocks.types.snow.SnowBlock
import de.bixilon.minosoft.data.registries.blocks.types.snow.SnowLayerBlock
import de.bixilon.minosoft.data.registries.blocks.types.stone.RockBlock
import de.bixilon.minosoft.data.registries.blocks.types.wood.*
import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries


object BlockFactories : DefaultFactory<BlockFactory<*>>(
    AirBlock.Air, AirBlock.VoidAir, AirBlock.CaveAir,

    RockBlock.Stone,
    RockBlock.Granite, RockBlock.PolishedGranite,
    RockBlock.Diorite, RockBlock.PolishedDiorite,
    RockBlock.Andesite, RockBlock.PolishedAndesite,

    GrassBlock,

    WaterFluidBlock, BubbleColumnBlock, LavaFluidBlock,

    SlimeBlock, HoneyBlock,

    CobwebBlock,

    WoolBlock.White, WoolBlock.Orange, WoolBlock.Magenta, WoolBlock.LightBlue, WoolBlock.Yellow, WoolBlock.Lime, WoolBlock.Pink, WoolBlock.Gray, WoolBlock.LightGray, WoolBlock.Cyan, WoolBlock.Purple, WoolBlock.Blue, WoolBlock.Brown, WoolBlock.Green, WoolBlock.Green, WoolBlock.Red, WoolBlock.Black,

    ScaffoldingBlock,

    PowderSnowBlock,


    Oak.Leaves,
    Spruce.Leaves,
    Birch.Leaves,
    Jungle.Leaves,
    Acacia.Leaves,
    DarkOak.Leaves,
    Mangrove.Leaves,
    Cherry.Leaves,
    Azalea.Leaves,
    FloweringAzalea.Leaves,

    SnowBlock, SnowLayerBlock,
) {

    fun build(name: ResourceLocation, registries: Registries, settings: BlockSettings): Block? {
        return this[name]?.build(registries, settings)
    }
}
