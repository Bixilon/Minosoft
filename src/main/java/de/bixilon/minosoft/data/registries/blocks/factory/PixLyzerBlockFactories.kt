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

import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.*
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.button.StoneButtonBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.button.WoodenButtonBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.*
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.end.EndGatewayBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.end.EndPortalBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.redstone.CommandBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.redstone.DaylightDetectorBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.redstone.PistonBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.redstone.SculkSensorBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.sign.StandingSignBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.sign.WallSignBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.plant.CropBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.plant.PlantBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.plant.SweetBerryBushBlock
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
    LeverBlock,
    NoteBlock,
    RepeaterBlock,
    ComparatorBlock,
    CampfireBlock,
    TorchBlock,
    NetherPortalBlock,
    RedstoneTorchBlock,
    KelpBlock,
    KelpPlantBlock,
    SeagrassBlock,
    StoneButtonBlock,
    WoodenButtonBlock,
    PlantBlock,
    CropBlock,
    CraftingTableBlock,

    WallSignBlock,
    StandingSignBlock,
    MobSpawnerBlock,
    PistonBlock,
    EnchantingTableBlock,
    EndPortalBlock,
    BeaconBlock,
    SkullBlock,
    DaylightDetectorBlock,
    BannerBlock,
    StructureBlock,
    EndGatewayBlock,
    CommandBlock,
    BedBlock,
    ConduitBlock,
    LecternBlock,
    BellBlock,
    JigsawBlock,
    CampfireBlock,
    BeehiveBlock,
    SculkSensorBlock,

    TrapdoorBlock,
    SweetBerryBushBlock,
    LadderBlock,
)
