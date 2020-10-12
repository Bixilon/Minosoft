/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.MapSet;
import de.bixilon.minosoft.game.datatypes.VersionValueMap;
import de.bixilon.minosoft.game.datatypes.entities.block.BlockEntityMetaData;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

public class PacketBlockEntityMetadata implements ClientboundPacket {
    BlockPosition position;
    BlockEntityActions action;
    BlockEntityMetaData data;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getProtocolId() < 6) {
            position = buffer.readBlockPositionShort();
            action = BlockEntityActions.byId(buffer.readByte(), buffer.getProtocolId());
            data = BlockEntityMetaData.getData(action, (CompoundTag) buffer.readNBT(true));
            return true;
        }
        position = buffer.readPosition();
        action = BlockEntityActions.byId(buffer.readByte(), buffer.getProtocolId());
        data = BlockEntityMetaData.getData(action, (CompoundTag) buffer.readNBT());
        return true;
    }


    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Receiving blockEntityMeta (position=%s, action=%s)", position, action));
    }

    public BlockPosition getPosition() {
        return position;
    }

    public BlockEntityActions getAction() {
        return action;
    }

    public BlockEntityMetaData getData() {
        return data;
    }

    public enum BlockEntityActions {
        SPAWNER(new MapSet[]{new MapSet<>(0, 1)}),
        COMMAND_BLOCK_TEXT(new MapSet[]{new MapSet<>(0, 2)}),
        BEACON(new MapSet[]{new MapSet<>(33, 3)}),
        SKULL(new MapSet[]{new MapSet<>(0, 3), new MapSet<>(33, 4)}),
        FLOWER_POT(new MapSet[]{new MapSet<>(0, 4), new MapSet<>(33, 5), new MapSet<>(346, -1)}),
        DECLARE_CONDUIT(new MapSet[]{new MapSet<>(371, 5)}),
        BANNER(new MapSet[]{new MapSet<>(30, 6)}),
        DATA_STRUCTURE_TILE_ENTITY(new MapSet[]{new MapSet<>(49, 7)}), // ToDo: was this really in 49?
        END_GATEWAY_DESTINATION(new MapSet[]{new MapSet<>(49, 8)}),
        SET_TEXT_ON_SIGN(new MapSet[]{new MapSet<>(110, 9)}),
        DECLARE_SHULKER_BOX(new MapSet[]{new MapSet<>(307, 10)}),
        SET_BED_COLOR(new MapSet[]{new MapSet<>(321, 11)}),
        SET_DATA_JIGSAW(new MapSet[]{new MapSet<>(445, 12)}),
        SET_ITEMS_IN_CAMPFIRE(new MapSet[]{new MapSet<>(452, 13)}),
        BEE_HIVE(new MapSet[]{new MapSet<>(550, 14)});

        final VersionValueMap<Integer> valueMap;


        BlockEntityActions(MapSet<Integer, Integer>[] values) {
            valueMap = new VersionValueMap<>(values, true);
        }

        public static BlockEntityActions byId(int id, int protocolId) {
            for (BlockEntityActions actions : values()) {
                if (actions.getId(protocolId) == id) {
                    return actions;
                }
            }
            return null;
        }

        public int getId(int protocolId) {
            Integer ret = valueMap.get(protocolId);
            if (ret == null) {
                return -2;
            }
            return ret;
        }
    }
}