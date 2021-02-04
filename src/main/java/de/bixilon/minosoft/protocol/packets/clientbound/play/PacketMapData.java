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

import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import java.util.ArrayList;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketMapData extends ClientboundPacket {
    int mapId;
    PacketMapDataDataActions dataData;

    // depends on data
    // start
    byte xStart;
    byte yStart;
    byte[] colors;

    // players
    ArrayList<MapPinSet> pins;

    boolean locked;
    // scale
    byte scale;

    byte[] data;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.mapId = buffer.readVarInt(); // mapId
        if (buffer.getVersionId() < V_14W28A) {
            int length = buffer.readUnsignedShort();
            // read action
            this.dataData = PacketMapDataDataActions.byId(buffer.readUnsignedByte());
            switch (this.dataData) {
                case START -> {
                    this.xStart = buffer.readByte();
                    this.yStart = buffer.readByte();
                    this.colors = buffer.readBytes(length - 3); // 3: dataData(1) + xStart (1) + yStart (1)
                }
                case PLAYERS -> {
                    this.pins = new ArrayList<>();
                    length--; // minus the dataData
                    for (int i = 0; i < length / 3; i++) { // loop over all sets ( 1 set: 3 bytes)
                        byte directionAndType = buffer.readByte();
                        byte x = buffer.readByte();
                        byte z = buffer.readByte();
                        this.pins.add(new MapPinSet(MapPinTypes.byId(directionAndType & 0xF), directionAndType >>> 4, x, z));
                    }
                }
                case SCALE -> this.scale = buffer.readByte();
            }
            return true;
        }
        this.scale = buffer.readByte();
        if (buffer.getVersionId() >= V_15W34A && buffer.getVersionId() < V_20W46A) {
            boolean trackPosition = buffer.readBoolean();
        }
        if (buffer.getVersionId() >= V_19W02A) {
            this.locked = buffer.readBoolean();
        }
        int pinCount = 0;
        if (buffer.getVersionId() < V_20W46A) {
            pinCount = buffer.readVarInt();
        } else {
            if (buffer.readBoolean()) {
                pinCount = buffer.readVarInt();
            }
        }
        this.pins = new ArrayList<>();

        for (int i = 0; i < pinCount; i++) {
            if (buffer.getVersionId() < V_18W19A) {
                byte directionAndType = buffer.readByte();
                byte x = buffer.readByte();
                byte z = buffer.readByte();
                if (buffer.getVersionId() >= V_1_12_2) { // ToDo
                    this.pins.add(new MapPinSet(MapPinTypes.byId(directionAndType >>> 4), directionAndType & 0xF, x, z));
                } else {
                    this.pins.add(new MapPinSet(MapPinTypes.byId(directionAndType & 0xF), directionAndType >>> 4, x, z));
                }
                continue;
            }
            MapPinTypes type = MapPinTypes.byId(buffer.readVarInt());
            byte x = buffer.readByte();
            byte z = buffer.readByte();
            byte direction = buffer.readByte();
            ChatComponent displayName = null;
            if (buffer.readBoolean()) {
                displayName = buffer.readChatComponent();
            }
            this.pins.add(new MapPinSet(type, direction, x, z, displayName));
        }

        short columns = buffer.readUnsignedByte();
        if (columns > 0) {
            short rows = buffer.readUnsignedByte();
            short xOffset = buffer.readUnsignedByte();
            short zOffset = buffer.readUnsignedByte();

            int dataLength = buffer.readVarInt();
            this.data = buffer.readBytes(dataLength);
        }
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received map meta data (mapId=%d)", this.mapId));
    }

    public PacketMapDataDataActions getDataData() {
        return this.dataData;
    }

    public byte getXStart() {
        return this.xStart;
    }

    public byte getYStart() {
        return this.yStart;
    }

    public byte[] getColors() {
        return this.colors;
    }

    public ArrayList<MapPinSet> getPins() {
        return this.pins;
    }

    public byte getScale() {
        return this.scale;
    }

    public enum PacketMapDataDataActions {
        START,
        PLAYERS,
        SCALE;

        private static final PacketMapDataDataActions[] MAP_DATA_DATA_ACTIONS = values();

        public static PacketMapDataDataActions byId(int id) {
            return MAP_DATA_DATA_ACTIONS[id];
        }
    }

    public enum MapPinTypes {
        WHITE_ARROW,
        GREEN_ARROW,
        RED_ARROW,
        BLUE_ARROW,
        WHITE_CROSS,
        RED_POINTER,
        WHITE_CIRCLE,
        BLUE_SQUARE,
        SMALL_WHITE_CIRCLE,
        MANSION,
        TEMPLE,
        WHITE_BANNER,
        ORANGE_BANNER,
        MAGENTA_BANNER,
        LIGHT_BLUE_BANNER,
        YELLOW_BANNER,
        LIME_BANNER,
        PINK_BANNER,
        GRAY_BANNER,
        LIGHT_GRAY_BANNER,
        CYAN_BANNER,
        PURPLE_BANNER,
        BLUE_BANNER,
        BROWN_BANNER,
        GREEN_BANNER,
        RED_BANNER,
        BLACK_BANNER,
        TREASURE_MARKER;

        private static final MapPinTypes[] MAP_PIN_TYPES = values();

        public static MapPinTypes byId(int id) {
            return MAP_PIN_TYPES[id];
        }
    }

    public static class MapPinSet {
        private final MapPinTypes type;
        private final byte direction;
        private final byte x;
        private final byte z;
        private final ChatComponent displayName;

        public MapPinSet(MapPinTypes type, int direction, byte x, byte z) {
            this.type = type;
            this.direction = (byte) direction;
            this.x = x;
            this.z = z;
            this.displayName = null;
        }

        public MapPinSet(MapPinTypes type, int direction, byte x, byte z, ChatComponent displayName) {
            this.type = type;
            this.direction = (byte) direction;
            this.x = x;
            this.z = z;
            this.displayName = displayName;
        }

        public MapPinTypes getType() {
            return this.type;
        }

        public byte getDirection() {
            return this.direction;
        }

        public byte getX() {
            return this.x;
        }

        public byte getZ() {
            return this.z;
        }
    }
}
