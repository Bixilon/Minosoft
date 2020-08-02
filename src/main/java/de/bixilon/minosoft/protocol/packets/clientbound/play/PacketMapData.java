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

import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.util.BitByte;

import java.util.ArrayList;

public class PacketMapData implements ClientboundPacket {
    int mapId;
    PacketMapDataData dataData;

    // depends on data
    // start
    byte xStart;
    byte yStart;
    byte[] colors;

    // players
    ArrayList<MapPinSet> pins;

    boolean locked = false;
    //scale
    byte scale;

    byte[] data;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getProtocolId() < 27) {
            mapId = buffer.readVarInt(); // mapId
            short length = buffer.readShort();
            // read action
            dataData = PacketMapDataData.byId(buffer.readByte());
            switch (dataData) {
                case START:
                    xStart = buffer.readByte();
                    yStart = buffer.readByte();
                    colors = buffer.readBytes(length - 3); // 3: dataData(1) + xStart (1) + yStart (1)
                    break;
                case PLAYERS:
                    pins = new ArrayList<>();
                    length--; // minus the dataData
                    for (int i = 0; i < length / 3; i++) { // loop over all sets ( 1 set: 3 bytes)
                        byte directionAndType = buffer.readByte();
                        byte x = buffer.readByte();
                        byte z = buffer.readByte();
                        pins.add(new MapPinSet(MapPinType.byId(directionAndType & 0xF), directionAndType >>> 4, x, z));
                    }
                    break;
                case SCALE:
                    scale = buffer.readByte();
                    break;
            }
            return true;
        }
        if (buffer.getProtocolId() < 373) {
            mapId = buffer.readVarInt();
            scale = buffer.readByte();
            if (buffer.getProtocolId() >= 58) {
                boolean trackPosition = buffer.readBoolean();
            }
            int pinCount = buffer.readVarInt();
            pins = new ArrayList<>();
            for (int i = 0; i < pinCount; i++) {
                byte directionAndType = buffer.readByte();
                byte x = buffer.readByte();
                byte z = buffer.readByte();
                if (buffer.getProtocolId() >= 340) { //ToDo
                    pins.add(new MapPinSet(MapPinType.byId(directionAndType >>> 4), directionAndType & 0xF, x, z));
                } else {
                    pins.add(new MapPinSet(MapPinType.byId(directionAndType & 0xF), directionAndType >>> 4, x, z));
                }
            }
            short columns = BitByte.byteToUShort(buffer.readByte());
            if (columns > 0) {
                byte rows = buffer.readByte();
                byte xOffset = buffer.readByte();
                byte zOffset = buffer.readByte();

                int dataLength = buffer.readVarInt();
                data = buffer.readBytes(dataLength);
            }
            return true;
        }
        mapId = buffer.readVarInt();
        scale = buffer.readByte();
        boolean trackPosition = buffer.readBoolean();
        if (buffer.getProtocolId() >= 452) {
            locked = buffer.readBoolean();
        }
        int pinCount = buffer.readVarInt();
        pins = new ArrayList<>();
        for (int i = 0; i < pinCount; i++) {
            MapPinType type = MapPinType.byId(buffer.readVarInt());
            byte x = buffer.readByte();
            byte z = buffer.readByte();
            byte direction = buffer.readByte();
            TextComponent displayName = null;
            if (buffer.readBoolean()) {
                displayName = buffer.readTextComponent();
            }
            pins.add(new MapPinSet(type, direction, x, z, displayName));
        }
        short columns = BitByte.byteToUShort(buffer.readByte());
        if (columns > 0) {
            byte rows = buffer.readByte();
            byte xOffset = buffer.readByte();
            byte zOffset = buffer.readByte();

            int dataLength = buffer.readVarInt();
            data = buffer.readBytes(dataLength);
        }
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received map meta data (mapId=%d)", mapId));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public PacketMapDataData getDataData() {
        return dataData;
    }

    public byte getXStart() {
        return xStart;
    }

    public byte getYStart() {
        return yStart;
    }

    public byte[] getColors() {
        return colors;
    }

    public ArrayList<MapPinSet> getPins() {
        return pins;
    }

    public byte getScale() {
        return scale;
    }

    public enum PacketMapDataData {
        START(0),
        PLAYERS(1),
        SCALE(2);

        final int id;

        PacketMapDataData(int id) {
            this.id = id;
        }

        public static PacketMapDataData byId(int id) {
            for (PacketMapDataData d : values()) {
                if (d.getId() == id) {
                    return d;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    public enum MapPinType {
        WHITE_ARROW(0),
        GREEN_ARROW(1),
        RED_ARROW(2),
        BLUE_ARROW(3),
        WHITE_CROSS(4),
        RED_POINTER(5),
        WHITE_CIRCLE(6),
        BLUE_SQUARE(7),
        SMALL_WHITE_CIRCLE(8),
        MANSION(8),
        TEMPLE(9),
        WHITE_BANNER(10),
        ORANGE_BANNER(11),
        MAGENTA_BANNER(12),
        LIGHT_BLUE_BANNER(13),
        YELLOW_BANNER(14),
        LIME_BANNER(15),
        PINK_BANNER(16),
        GRAY_BANNER(17),
        LIGHT_GRAY_BANNER(18),
        CYAN_BANNER(19),
        PURPLE_BANNER(20),
        BLUE_BANNER(21),
        BROWN_BANNER(22),
        GREEN_BANNER(23),
        RED_BANNER(24),
        BLACK_BANNER(25),
        TREASURE_MARKER(26);

        final int id;

        MapPinType(int id) {
            this.id = id;
        }

        public static MapPinType byId(int id) {
            for (MapPinType type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return BLUE_SQUARE;
        }

        public int getId() {
            return id;
        }
    }

    public static class MapPinSet {
        final MapPinType type;
        final byte direction;
        final byte x;
        final byte z;
        final TextComponent displayName;

        public MapPinSet(MapPinType type, int direction, byte x, byte z) {
            this.type = type;
            this.direction = (byte) direction;
            this.x = x;
            this.z = z;
            displayName = null;
        }

        public MapPinSet(MapPinType type, int direction, byte x, byte z, TextComponent displayName) {
            this.type = type;
            this.direction = (byte) direction;
            this.x = x;
            this.z = z;
            this.displayName = displayName;
        }

        public MapPinType getType() {
            return type;
        }

        public byte getDirection() {
            return direction;
        }

        public byte getX() {
            return x;
        }

        public byte getZ() {
            return z;
        }
    }
}
