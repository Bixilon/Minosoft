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
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.nbt.tag.CompoundTag;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketBlockEntityMetadata implements ClientboundPacket {
    BlockPosition position;
    Actions action;
    CompoundTag nbt;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                position = buffer.readBlockPositionShort();
                action = Actions.byId(buffer.readByte(), buffer.getVersion());
                nbt = buffer.readNBT(true);
                return true;
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
                position = buffer.readPosition();
                action = Actions.byId(buffer.readByte(), buffer.getVersion());
                nbt = buffer.readNBT();
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Receiving blockEntityMeta (position=%s, action=%s)", position.toString(), action.name()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public BlockPosition getPosition() {
        return position;
    }


    public CompoundTag getNbt() {
        return nbt;
    }


    public enum Actions {
        SPAWNER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 1)}),
        COMMAND_BLOCK_TEXT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 2)}),
        BEACON(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_8, 3)}),
        SKULL(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 3), new MapSet<>(ProtocolVersion.VERSION_1_8, 4)}),
        FLOWER_POT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 4), new MapSet<>(ProtocolVersion.VERSION_1_8, 5)}),
        BANNER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_8, 6)}),
        DATA_STRUCTURE_TILE_ENTITY(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 7)}),
        END_GATEWAY_DESTINATION(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 8)}),
        SET_TEXT_ON_SIGN(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 9)}),
        DECLARE_SHULKER_BOX(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_11_2, 10)});

        final VersionValueMap<Integer> valueMap;

        Actions(MapSet<ProtocolVersion, Integer>[] values) {
            valueMap = new VersionValueMap<>(values, true);
        }

        public static Actions byId(int id, ProtocolVersion version) {
            for (Actions actions : values()) {
                if (actions.getId(version) == id) {
                    return actions;
                }
            }
            return null;
        }

        public int getId(ProtocolVersion version) {
            Integer ret = valueMap.get(version);
            if (ret == null) {
                return -2;
            }
            return ret;
        }
    }
}