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
import de.bixilon.minosoft.data.registries.blocks.types.building.brick.*
import de.bixilon.minosoft.data.registries.blocks.types.building.copper.*
import de.bixilon.minosoft.data.registries.blocks.types.building.dirt.GrassBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.door.DoorBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.end.EndStoneBrick
import de.bixilon.minosoft.data.registries.blocks.types.building.end.Purpur
import de.bixilon.minosoft.data.registries.blocks.types.building.plants.DoublePlant
import de.bixilon.minosoft.data.registries.blocks.types.building.plants.FernBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.prismarine.DarkPrismarine
import de.bixilon.minosoft.data.registries.blocks.types.building.prismarine.Prismarine
import de.bixilon.minosoft.data.registries.blocks.types.building.prismarine.PrismarineBrick
import de.bixilon.minosoft.data.registries.blocks.types.building.quartz.QuartzBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.quartz.SmoothQuartz
import de.bixilon.minosoft.data.registries.blocks.types.building.snow.SnowBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.snow.SnowLayerBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.*
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.sand.*
import de.bixilon.minosoft.data.registries.blocks.types.climbing.ScaffoldingBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.storage.EnderChestBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.storage.ShulkerBoxBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.storage.WoodenChestBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.LavaFluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.BubbleColumnBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.WaterFluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.SlimeBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.snow.PowderSnowBlock
import de.bixilon.minosoft.data.registries.blocks.types.pvp.CobwebBlock
import de.bixilon.minosoft.data.registries.blocks.types.wood.*
import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries


object BlockFactories : DefaultFactory<BlockFactory<*>>(
    AirBlock.Air, AirBlock.VoidAir, AirBlock.CaveAir,

    StoneBlock.Block, StoneBlock.Slab,
    SmoothStone.Block, SmoothStone.Slab,
    StoneBrick.Block, StoneBrick.Slab,
    Cobblestone.Block, Cobblestone.Slab,
    Granite.Block, Granite.Slab,
    PolishedGranite.Block, PolishedGranite.Slab,
    Diorite.Block, Diorite.Slab,
    PolishedDiorite.Block, PolishedDiorite.Slab,
    Andesite.Block, Andesite.Slab,
    PolishedAndesite.Block, PolishedAndesite.Slab,

    Sandstone.Block, Sandstone.Slab,
    CutSandstone.Block, CutSandstone.Slab,
    SmoothSandstone.Block, SmoothSandstone.Slab,
    RedSandstone.Block, RedSandstone.Slab,
    CutRedSandstone.Block, CutRedSandstone.Slab,
    SmoothRedSandstone.Block, SmoothRedSandstone.Slab,

    QuartzBlock.Block, QuartzBlock.Slab,
    SmoothQuartz.Block, SmoothQuartz.Slab,

    Brick.Block, Brick.Slab,
    NetherBrick.Block, NetherBrick.Slab,
    MudBrick.Block, MudBrick.Slab,
    MossyStoneBrick.Block, MossyStoneBrick.Slab,
    MossyCobblestone.Block, MossyCobblestone.Slab,
    RedNetherBrick.Block, RedNetherBrick.Slab,


    Prismarine.Block, Prismarine.Slab,
    PrismarineBrick.Block, PrismarineBrick.Slab,
    DarkPrismarine.Block, DarkPrismarine.Slab,

    EndStoneBrick.Block, EndStoneBrick.Slab,
    Purpur.Block, Purpur.Slab,


    OxidizedCopper.Block, OxidizedCopper.Slab,
    OxidizedCutCopper.Block, OxidizedCutCopper.Slab,
    WaxedOxidizedCutCopper.Block, WaxedOxidizedCutCopper.Slab,
    WeatheredCopper.Block, WeatheredCopper.Slab,
    WeatheredCutCopper.Block, WeatheredCutCopper.Slab,
    WaxedWeatheredCutCopper.Block, WaxedWeatheredCutCopper.Slab,
    ExposedCopper.Block, ExposedCopper.Slab,
    ExposedCutCopper.Block, ExposedCutCopper.Slab,
    WaxedExposedCutCopper.Block, WaxedExposedCutCopper.Slab,
    CutCopper.Block, CutCopper.Slab,
    WaxedCutCopper.Block, WaxedCutCopper.Slab,

    GrassBlock,

    WaterFluidBlock, BubbleColumnBlock, LavaFluidBlock,

    SlimeBlock, HoneyBlock,

    CobwebBlock,

    WoolBlock.White, WoolBlock.Orange, WoolBlock.Magenta, WoolBlock.LightBlue, WoolBlock.Yellow, WoolBlock.Lime, WoolBlock.Pink, WoolBlock.Gray, WoolBlock.LightGray, WoolBlock.Cyan, WoolBlock.Purple, WoolBlock.Blue, WoolBlock.Brown, WoolBlock.Green, WoolBlock.Green, WoolBlock.Red, WoolBlock.Black,

    ScaffoldingBlock,

    PowderSnowBlock,

    DoorBlock.IronDoor,

    Oak.Leaves, Oak.Door, Oak.Slab,
    Spruce.Leaves, Spruce.Door, Spruce.Slab,
    Birch.Leaves, Birch.Door, Birch.Slab,
    Jungle.Leaves, Jungle.Door, Jungle.Slab,
    Acacia.Leaves, Acacia.Door, Acacia.Slab,
    DarkOak.Leaves, DarkOak.Door, DarkOak.Slab,
    Mangrove.Leaves, Mangrove.Door, Mangrove.Slab,
    Cherry.Leaves, Cherry.Door, Cherry.Slab,
    Azalea.Leaves, Azalea.Door, Azalea.Slab,
    FloweringAzalea.Leaves, FloweringAzalea.Door, FloweringAzalea.Slab,
    Bamboo.Leaves, Bamboo.Door, Bamboo.Slab,
    BambooMosaic.Leaves, BambooMosaic.Door, BambooMosaic.Slab,
    Crimson.Leaves, Crimson.Door, Crimson.Slab,
    Warped.Leaves, Warped.Door, Warped.Slab,

    SnowBlock, SnowLayerBlock,
    FernBlock.DeadBush, FernBlock.Grass, FernBlock.ShortGrass, FernBlock.Fern,
    DoublePlant.Sunflower, DoublePlant.Lilac, DoublePlant.TallGrass, DoublePlant.LargeFern, DoublePlant.RoseBush, DoublePlant.Peony, DoublePlant.UpperBlock,


    WoodenChestBlock.Chest, WoodenChestBlock.TrappedChest, EnderChestBlock,
    ShulkerBoxBlock, ShulkerBoxBlock.White, ShulkerBoxBlock.Orange, ShulkerBoxBlock.Magenta, ShulkerBoxBlock.LightBlue, ShulkerBoxBlock.Yellow, ShulkerBoxBlock.Lime, ShulkerBoxBlock.Pink, ShulkerBoxBlock.Gray, ShulkerBoxBlock.LightGray, ShulkerBoxBlock.Cyan, ShulkerBoxBlock.Purple, ShulkerBoxBlock.Blue, ShulkerBoxBlock.Brown, ShulkerBoxBlock.Green, ShulkerBoxBlock.Green, ShulkerBoxBlock.Red, ShulkerBoxBlock.Black,
) {

    fun build(name: ResourceLocation, registries: Registries, settings: BlockSettings): Block? {
        return this[name]?.build(registries, settings)
    }
}
