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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.MapSet;
import de.bixilon.minosoft.data.VersionValueMap;
import de.bixilon.minosoft.data.entities.block.BlockEntityMetaData;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.modding.event.events.BlockEntityMetaDataChangeEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

import static de.bixilon.minosoft.protocol.protocol.Versions.*;

public class PacketBlockEntityMetadata extends ClientboundPacket {
    BlockPosition position;
    BlockEntityActions action;
    BlockEntityMetaData data;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < V_15W41A3B) {
            this.position = buffer.readBlockPositionShort();
            this.action = BlockEntityActions.byId(buffer.readUnsignedByte(), buffer.getVersionId());
            this.data = BlockEntityMetaData.getData(this.action, (CompoundTag) buffer.readNBT(true));
            return true;
        }
        this.position = buffer.readPosition();
        this.action = BlockEntityActions.byId(buffer.readUnsignedByte(), buffer.getVersionId());
        this.data = BlockEntityMetaData.getData(this.action, (CompoundTag) buffer.readNBT());
        return true;
    }

    @Override
    public void handle(Connection connection) {
        connection.fireEvent(new BlockEntityMetaDataChangeEvent(connection, this));
        connection.getPlayer().getWorld().setBlockEntityData(getPosition(), getData());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Receiving blockEntityMeta (position=%s, action=%s)", this.position, this.action));
    }

    public BlockPosition getPosition() {
        return this.position;
    }

    public BlockEntityActions getAction() {
        return this.action;
    }

    public BlockEntityMetaData getData() {
        return this.data;
    }

    public enum BlockEntityActions {
        SPAWNER(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1)}),
        COMMAND_BLOCK_TEXT(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 2)}),
        BEACON(new MapSet[]{new MapSet<>(V_14W32A, 3)}),
        SKULL(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 3), new MapSet<>(V_14W32A, 4)}),
        FLOWER_POT(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 4), new MapSet<>(V_14W32A, 5), new MapSet<>(V_17W47A, -1)}),
        DECLARE_CONDUIT(new MapSet[]{new MapSet<>(V_18W15A, 5)}),
        BANNER(new MapSet[]{new MapSet<>(V_14W30B, 6)}),
        DATA_STRUCTURE_TILE_ENTITY(new MapSet[]{new MapSet<>(V_15W31A, 7)}), // ToDo: was this really in 49?
        END_GATEWAY_DESTINATION(new MapSet[]{new MapSet<>(V_15W31A, 8)}),
        SET_TEXT_ON_SIGN(new MapSet[]{new MapSet<>(V_1_9_4, 9)}),
        DECLARE_SHULKER_BOX(new MapSet[]{new MapSet<>(V_16W39A, 10)}),
        SET_BED_COLOR(new MapSet[]{new MapSet<>(V_17W15A, 11)}),
        SET_DATA_JIGSAW(new MapSet[]{new MapSet<>(V_18W46A, 12)}),
        SET_ITEMS_IN_CAMPFIRE(new MapSet[]{new MapSet<>(V_19W02A, 13)}),
        BEE_HIVE(new MapSet[]{new MapSet<>(V_19W34A, 14)});

        final VersionValueMap<Integer> valueMap;

        BlockEntityActions(MapSet<Integer, Integer>[] values) {
            this.valueMap = new VersionValueMap<>(values);
        }

        public static BlockEntityActions byId(int id, int versionId) {
            for (BlockEntityActions actions : values()) {
                if (actions.getId(versionId) == id) {
                    return actions;
                }
            }
            return null;
        }

        public int getId(int versionId) {
            Integer ret = this.valueMap.get(versionId);
            if (ret == null) {
                return -2;
            }
            return ret;
        }
    }
}
