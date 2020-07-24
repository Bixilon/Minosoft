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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketUpdateStructureBlock implements ServerboundPacket {
    final BlockPosition position;
    final StructureBlockActions action;
    final StructureBlockModes mode;
    final String name;

    final byte offsetX;
    final byte offsetY;
    final byte offsetZ;

    final byte sizeX;
    final byte sizeY;
    final byte sizeZ;

    final StructureBlockMirrors mirror;
    final StructureBlockRotations rotation;
    final String metaData;
    final float integrity;
    final long seed;
    final byte flags;

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
    public OutPacketBuffer write(ProtocolVersion version) {
        OutPacketBuffer buffer = new OutPacketBuffer(version, version.getPacketCommand(Packets.Serverbound.PLAY_UPDATE_STRUCTURE_BLOCK));
        switch (version) {
            case VERSION_1_13_2:
            case VERSION_1_14_4:
                buffer.writePosition(position);
                buffer.writeVarInt(action.getId());
                buffer.writeVarInt(mode.getId());
                buffer.writeString(name);
                buffer.writeByte(offsetX);
                buffer.writeByte(offsetY);
                buffer.writeByte(offsetZ);
                buffer.writeByte(sizeX);
                buffer.writeByte(sizeY);
                buffer.writeByte(sizeZ);
                buffer.writeVarInt(mirror.getId());
                buffer.writeVarInt(rotation.getId());
                buffer.writeString(metaData);
                buffer.writeFloat(integrity);
                buffer.writeVarLong(seed);
                buffer.writeByte(flags);
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending update structure block packet (position=%s, action=%s, mode=%s, name=\"%s\", offsetX=%d, offsetY=%d, offsetZ=%d, sizeX=%d, sizeY=%d, sizeZ=%d, mirror=%s, rotation=%s, metaData=\"%s\", integrity=%s, seed=%s, flags=%s)", position, action, mode, name, offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, mirror, rotation, metaData, integrity, seed, flags));
    }

    public enum StructureBlockActions {
        UPDATE(0),
        SAVE(1),
        LOAD(2),
        DETECT_SIZE(3);

        final int id;

        StructureBlockActions(int id) {
            this.id = id;
        }

        public static StructureBlockActions byId(int id) {
            for (StructureBlockActions action : values()) {
                if (action.getId() == id) {
                    return action;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    public enum StructureBlockModes {
        SAVE(0),
        LOAD(1),
        CORNER(2),
        DATA(3);

        final int id;

        StructureBlockModes(int id) {
            this.id = id;
        }

        public static StructureBlockModes byId(int id) {
            for (StructureBlockModes mode : values()) {
                if (mode.getId() == id) {
                    return mode;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    public enum StructureBlockMirrors {
        NONE(0),
        LEFT_RIGHT(1),
        FRONT_BACK(2);

        final int id;

        StructureBlockMirrors(int id) {
            this.id = id;
        }

        public static StructureBlockMirrors byId(int id) {
            for (StructureBlockMirrors mirror : values()) {
                if (mirror.getId() == id) {
                    return mirror;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    public enum StructureBlockRotations {
        NONE(0),
        CLOCKWISE_90(1),
        CLOCKWISE_180(2),
        COUNTERCLOCKWISE_90(3);


        final int id;

        StructureBlockRotations(int id) {
            this.id = id;
        }

        public static StructureBlockRotations byId(int id) {
            for (StructureBlockRotations rotation : values()) {
                if (rotation.getId() == id) {
                    return rotation;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

}
