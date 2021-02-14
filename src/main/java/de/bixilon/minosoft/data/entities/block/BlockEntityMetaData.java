/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.block;

import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketBlockEntityMetadata;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

import javax.annotation.Nullable;

public abstract class BlockEntityMetaData {
    public static BlockEntityMetaData getData(Connection connection, @Nullable PacketBlockEntityMetadata.BlockEntityActions action, CompoundTag nbt) {
        String item;
        if (action != null) {
            item = action.name();
        } else
            // new format, use id in nbt
            if (nbt.containsKey("id")) {
                item = nbt.getStringTag("id").getValue();
            } else {
                return null;
            }

        return switch (item) { // ToDo: https://minecraft.gamepedia.com/Block_entity
            case "minecraft:bed", "SET_BED_COLOR" -> new BedEntityMetaData(connection, nbt.getTag("color"));
            case "minecraft:campfire", "SET_ITEMS_IN_CAMPFIRE" -> new CampfireBlockEntityMetaData(connection, nbt.getListTag("Items"));
            default -> null;
        };
    }
}
