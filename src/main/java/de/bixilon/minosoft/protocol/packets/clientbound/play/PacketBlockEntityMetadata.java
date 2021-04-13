/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.VersionValueMap;
import de.bixilon.minosoft.data.entities.block.BlockEntityMetaData;
import de.bixilon.minosoft.modding.event.events.BlockEntityMetaDataChangeEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;
import glm_.vec3.Vec3i;

import java.util.Map;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketBlockEntityMetadata extends PlayClientboundPacket {
    private final Vec3i position;
    private final BlockEntityActions action;
    private final BlockEntityMetaData data;

    public PacketBlockEntityMetadata(PlayInByteBuffer buffer) {
        if (buffer.getVersionId() < V_14W03B) {
            this.position = buffer.readBlockPositionShort();
            this.action = BlockEntityActions.byId(buffer.readUnsignedByte(), buffer.getVersionId());
            this.data = BlockEntityMetaData.getData(buffer.getConnection(), this.action, (CompoundTag) buffer.readNBT(true));
            return;
        }
        this.position = buffer.readBlockPosition();
        this.action = BlockEntityActions.byId(buffer.readUnsignedByte(), buffer.getVersionId());
        this.data = BlockEntityMetaData.getData(buffer.getConnection(), this.action, (CompoundTag) buffer.readNBT());
    }

    @Override
    public void handle(PlayConnection connection) {
        connection.fireEvent(new BlockEntityMetaDataChangeEvent(connection, this));
        connection.getWorld().setBlockEntityData(getPosition(), getData());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Receiving blockEntityMeta (position=%s, action=%s)", this.position, this.action));
    }

    public Vec3i getPosition() {
        return this.position;
    }

    public BlockEntityActions getAction() {
        return this.action;
    }

    public BlockEntityMetaData getData() {
        return this.data;
    }

    public enum BlockEntityActions {
        SPAWNER(Map.of(LOWEST_VERSION_SUPPORTED, 1)),
        COMMAND_BLOCK_TEXT(Map.of(LOWEST_VERSION_SUPPORTED, 2)),
        BEACON(Map.of(V_14W32A, 3)),
        SKULL(Map.of(LOWEST_VERSION_SUPPORTED, 3, V_14W32A, 4)),
        FLOWER_POT(Map.of(LOWEST_VERSION_SUPPORTED, 4, V_14W32A, 5, V_17W47A, -1)),
        DECLARE_CONDUIT(Map.of(V_18W15A, 5)),
        BANNER(Map.of(V_14W30B, 6)),
        DATA_STRUCTURE_TILE_ENTITY(Map.of(V_15W31A, 7)), // ToDo: was this really in 49?
        END_GATEWAY_DESTINATION(Map.of(V_15W31A, 8)),
        SET_TEXT_ON_SIGN(Map.of(V_1_9_4, 9)),
        DECLARE_SHULKER_BOX(Map.of(V_16W39A, 10)),
        SET_BED_COLOR(Map.of(V_17W15A, 11)),
        SET_DATA_JIGSAW(Map.of(V_18W46A, 12)),
        SET_ITEMS_IN_CAMPFIRE(Map.of(V_19W02A, 13)),
        BEE_HIVE(Map.of(V_19W34A, 14));

   private final VersionValueMap<Integer> valueMap;

        BlockEntityActions(Map<Integer, Integer> values) {
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
