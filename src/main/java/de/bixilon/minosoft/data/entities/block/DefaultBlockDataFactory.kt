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

package de.bixilon.minosoft.data.entities.block

import de.bixilon.minosoft.data.entities.block.container.storage.ChestBlockEntity
import de.bixilon.minosoft.data.entities.block.container.storage.EnderChestBlockEntity
import de.bixilon.minosoft.data.entities.block.container.storage.ShulkerBoxBlockEntity
import de.bixilon.minosoft.data.entities.block.container.storage.TrappedChestBlockEntity
import de.bixilon.minosoft.data.entities.block.end.EndGatewayBlockEntity
import de.bixilon.minosoft.data.entities.block.end.EndPortalBlockEntity
import de.bixilon.minosoft.data.entities.block.redstone.CommandBlockEntity
import de.bixilon.minosoft.data.entities.block.redstone.SculkSensorBlockEntity
import de.bixilon.minosoft.data.entities.block.redstone.piston.PistonBlockEntity
import de.bixilon.minosoft.data.entities.block.redstone.piston.StickyPistonBlockEntity
import de.bixilon.minosoft.data.entities.block.sign.SignBlockEntity
import de.bixilon.minosoft.data.registries.factory.DefaultFactory

object DefaultBlockDataFactory : DefaultFactory<BlockEntityFactory<out BlockEntity>>(
    BedBlockEntity,
    SignBlockEntity,
    CampfireBlockEntity,
    JigsawBlockEntity,
    LecternBlockEntity,
    BellBlockEntity,
    ConduitBlockEntity,
    ShulkerBoxBlockEntity,
    StructureBlockBlockEntity,
    CommandBlockEntity,
    BannerBlockEntity,
    BeaconBlockEntity,
    SkullBlockEntity,
    EnchantingTableBlockEntity,
    MobSpawnerBlockEntity,
    EnderChestBlockEntity,
    JukeboxBlockEntity,
    ChestBlockEntity,
    TrappedChestBlockEntity,
    BeehiveBlockEntity,
    NoteBlockBlockEntity,
    EndGatewayBlockEntity,
    PistonBlockEntity,
    StickyPistonBlockEntity,
    FlowerPotBlockEntity,
    EndPortalBlockEntity,
    SculkSensorBlockEntity,
)
