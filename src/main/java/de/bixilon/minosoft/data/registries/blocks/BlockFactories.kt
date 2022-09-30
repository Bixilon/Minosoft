/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks

import de.bixilon.minosoft.data.registries.blocks.types.*
import de.bixilon.minosoft.data.registries.blocks.types.button.StoneButtonBlock
import de.bixilon.minosoft.data.registries.blocks.types.button.WoodenButtonBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.*
import de.bixilon.minosoft.data.registries.blocks.types.entity.container.DispenserBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.container.DropperBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.container.HopperBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.container.SmokerBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.container.processing.BlastFurnaceBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.container.processing.BrewingStandBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.container.processing.FurnaceBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.container.storage.BarrelBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.container.storage.ChestBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.container.storage.EnderChestBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.container.storage.TrappedChestBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.end.EndGatewayBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.end.EndPortalBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.redstone.CommandBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.redstone.DaylightDetectorBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.redstone.PistonBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.redstone.SculkSensorBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.sign.StandingSignBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.sign.WallSignBlock
import de.bixilon.minosoft.data.registries.blocks.types.leaves.LeavesBlock
import de.bixilon.minosoft.data.registries.blocks.types.plant.CropBlock
import de.bixilon.minosoft.data.registries.blocks.types.plant.PlantBlock
import de.bixilon.minosoft.data.registries.blocks.types.portal.NetherPortalBlock
import de.bixilon.minosoft.data.registries.blocks.types.redstone.ComparatorBlock
import de.bixilon.minosoft.data.registries.blocks.types.redstone.RepeaterBlock
import de.bixilon.minosoft.data.registries.blocks.types.wall.LeverBlock
import de.bixilon.minosoft.data.registries.blocks.types.water.KelpBlock
import de.bixilon.minosoft.data.registries.blocks.types.water.KelpPlantBlock
import de.bixilon.minosoft.data.registries.blocks.types.water.SeagrassBlock
import de.bixilon.minosoft.data.registries.factory.clazz.DefaultClassFactory

object BlockFactories : DefaultClassFactory<BlockFactory<*>>(
    AirBlock,
    Block,
    FluidBlock,
    DoorBlock,
    LeverBlock,
    NoteBlock,
    RepeaterBlock,
    ComparatorBlock,
    CampfireBlock,
    TorchBlock,
    SlimeBlock,
    BrewingStandBlock,
    EnderChestBlock,
    NetherPortalBlock,
    RedstoneTorchBlock,
    HoneyBlock,
    KelpBlock,
    KelpPlantBlock,
    SeagrassBlock,
    StoneButtonBlock,
    WoodenButtonBlock,
    LeavesBlock,
    PlantBlock,
    CropBlock,
    CraftingTableBlock,

    FurnaceBlock,
    ChestBlock,
    TrappedChestBlock,
    EnderChestBlock,
    JukeboxBlock,
    DispenserBlock,
    DropperBlock,
    WallSignBlock,
    StandingSignBlock,
    MobSpawnerBlock,
    PistonBlock,
    BrewingStandBlock,
    EnchantingTableBlock,
    EndPortalBlock,
    BeaconBlock,
    SkullBlock,
    DaylightDetectorBlock,
    HopperBlock,
    BannerBlock,
    StructureBlock,
    EndGatewayBlock,
    CommandBlock,
    ShulkerBoxBlock,
    BedBlock,
    ConduitBlock,
    BarrelBlock,
    SmokerBlock,
    BlastFurnaceBlock,
    LecternBlock,
    BellBlock,
    JigsawBlock,
    CampfireBlock,
    BeehiveBlock,
    SculkSensorBlock,
)
