/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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
import de.bixilon.minosoft.data.registries.blocks.types.leaves.LeavesBlock
import de.bixilon.minosoft.data.registries.blocks.types.portal.NetherPortalBlock
import de.bixilon.minosoft.data.registries.blocks.types.redstone.ComparatorBlock
import de.bixilon.minosoft.data.registries.blocks.types.redstone.RepeaterBlock
import de.bixilon.minosoft.data.registries.blocks.types.wall.LeverBlock
import de.bixilon.minosoft.data.registries.blocks.types.water.KelpBlock
import de.bixilon.minosoft.data.registries.blocks.types.water.SeagrassBlock
import de.bixilon.minosoft.data.registries.factory.clazz.DefaultClassFactory

object DefaultBlockFactories : DefaultClassFactory<BlockFactory<*>>(
    FluidBlock,
    DoorBlock,
    LeverBlock,
    NoteBlock,
    RepeaterBlock,
    ComparatorBlock,
    CampfireBlock,
    TorchBlock,
    SlimeBlock,
    BedBlock,
    BrewingStandBlock,
    EnderChestBlock,
    NetherPortalBlock,
    RedstoneTorchBlock,
    HoneyBlock,
    KelpBlock,
    SeagrassBlock,
    StoneButtonBlock,
    WoodenButtonBlock,
    LeavesBlock,
)
