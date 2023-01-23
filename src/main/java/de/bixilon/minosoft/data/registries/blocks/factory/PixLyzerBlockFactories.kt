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

package de.bixilon.minosoft.data.registries.blocks.factory

import de.bixilon.minosoft.data.registries.blocks.types.*
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.*
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.button.StoneButtonBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.button.WoodenButtonBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.*
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.container.DispenserBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.container.DropperBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.container.HopperBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.container.SmokerBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.container.processing.BlastFurnaceBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.container.processing.BrewingStandBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.container.processing.FurnaceBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.container.storage.BarrelBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.container.storage.ChestBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.container.storage.EnderChestBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.container.storage.TrappedChestBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.end.EndGatewayBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.end.EndPortalBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.redstone.CommandBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.redstone.DaylightDetectorBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.redstone.PistonBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.redstone.SculkSensorBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.sign.StandingSignBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.sign.WallSignBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.leaves.LeavesBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.plant.CropBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.plant.PlantBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.portal.NetherPortalBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.redstone.ComparatorBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.redstone.RepeaterBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.wall.LeverBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.water.KelpBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.water.KelpPlantBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.water.SeagrassBlock
import de.bixilon.minosoft.data.registries.factory.clazz.DefaultClassFactory

@Deprecated("BlockFactories")
object PixLyzerBlockFactories : DefaultClassFactory<PixLyzerBlockFactory<*>>(
    PixLyzerBlock,
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
