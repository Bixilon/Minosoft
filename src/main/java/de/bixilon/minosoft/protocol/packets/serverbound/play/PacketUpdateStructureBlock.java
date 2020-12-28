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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

public class PacketUpdateStructureBlock implements ServerboundPacket {
    private final BlockPosition position;
    private final StructureBlockActions action;
    private final StructureBlockModes mode;
    private final String name;
    private final byte offsetX;
    private final byte offsetY;
    private final byte offsetZ;
    private final byte sizeX;
    private final byte sizeY;
    private final byte sizeZ;
    private final StructureBlockMirrors mirror;
    private final StructureBlockRotations rotation;
    private final String metaData;
    private final float integrity;
    private final long seed;
    private final byte flags;

    public PacketUpdateStructureBlock(BlockPosition position, StructureBlockActions action, StructureBlockModes mode, String name, byte offsetX, byte offsetY, byte offsetZ, byte sizeX, byte sizeY, byte sizeZ, StructureBlockMirrors mirror, StructureBlockRotations rotation, String metaData, float integrity, long seed, byte flags) {
        this.position = position;
        this.action = action;
        this.mode = mode;
        this.name = name;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.mirror = mirror;
        this.rotation = rotation;
        this.metaData = metaData;
        this.integrity = integrity;
        this.seed = seed;
        this.flags = flags;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_UPDATE_STRUCTURE_BLOCK);
        buffer.writePosition(this.position);
        buffer.writeVarInt(this.action.ordinal());
        buffer.writeVarInt(this.mode.ordinal());
        buffer.writeString(this.name);
        buffer.writeByte(this.offsetX);
        buffer.writeByte(this.offsetY);
        buffer.writeByte(this.offsetZ);
        buffer.writeByte(this.sizeX);
        buffer.writeByte(this.sizeY);
        buffer.writeByte(this.sizeZ);
        buffer.writeVarInt(this.mirror.ordinal());
        buffer.writeVarInt(this.rotation.ordinal());
        buffer.writeString(this.metaData);
        buffer.writeFloat(this.integrity);
        buffer.writeVarLong(this.seed);
        buffer.writeByte(this.flags);
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending update structure block packet (position=%s, action=%s, mode=%s, name=\"%s\", offsetX=%d, offsetY=%d, offsetZ=%d, sizeX=%d, sizeY=%d, sizeZ=%d, mirror=%s, rotation=%s, metaData=\"%s\", integrity=%s, seed=%s, flags=%s)", this.position, this.action, this.mode, this.name, this.offsetX, this.offsetY, this.offsetZ, this.sizeX, this.sizeY, this.sizeZ, this.mirror, this.rotation, this.metaData, this.integrity, this.seed, this.flags));
    }

    public enum StructureBlockActions {
        UPDATE,
        SAVE,
        LOAD,
        DETECT_SIZE;

        private static final StructureBlockActions[] STRUCTURE_BLOCK_ACTIONS = values();

        public static StructureBlockActions byId(int id) {
            return STRUCTURE_BLOCK_ACTIONS[id];
        }
    }

    public enum StructureBlockModes {
        SAVE,
        LOAD,
        CORNER,
        DATA;

        private static final StructureBlockModes[] STRUCTURE_BLOCK_MODES = values();

        public static StructureBlockModes byId(int id) {
            return STRUCTURE_BLOCK_MODES[id];
        }
    }

    public enum StructureBlockMirrors {
        NONE,
        LEFT_RIGHT,
        FRONT_BACK;

        private static final StructureBlockMirrors[] STRUCTURE_BLOCK_MIRRORS = values();

        public static StructureBlockMirrors byId(int id) {
            return STRUCTURE_BLOCK_MIRRORS[id];
        }
    }

    public enum StructureBlockRotations {
        NONE,
        CLOCKWISE_90,
        CLOCKWISE_180,
        COUNTERCLOCKWISE_90;

        private static final StructureBlockRotations[] STRUCTURE_BLOCK_ROTATIONS = values();

        public static StructureBlockRotations byId(int id) {
            return STRUCTURE_BLOCK_ROTATIONS[id];
        }
    }

}
