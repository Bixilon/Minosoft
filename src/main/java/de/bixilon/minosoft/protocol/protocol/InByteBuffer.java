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

package de.bixilon.minosoft.protocol.protocol;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bixilon.minosoft.data.Directions;
import de.bixilon.minosoft.data.commands.CommandArgumentNode;
import de.bixilon.minosoft.data.commands.CommandLiteralNode;
import de.bixilon.minosoft.data.commands.CommandNode;
import de.bixilon.minosoft.data.commands.CommandRootNode;
import de.bixilon.minosoft.data.entities.Poses;
import de.bixilon.minosoft.data.mappings.LegacyResourceLocation;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.network.connection.Connection;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.nbt.tag.*;
import glm_.vec2.Vec2i;
import glm_.vec3.Vec3;
import glm_.vec3.Vec3i;
import org.checkerframework.common.value.qual.IntRange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class InByteBuffer {
    private final Connection connection;
    private final byte[] bytes;
    private int position;

    public InByteBuffer(byte[] bytes, Connection connection) {
        this.bytes = bytes;
        this.connection = connection;
    }

    public InByteBuffer(InByteBuffer buffer) {
        this.bytes = buffer.getBytes();
        this.position = buffer.getPosition();
        this.connection = buffer.getConnection();
    }

    public short readShort() {
        return (short) (((readUnsignedByte()) << 8) | (readUnsignedByte()));
    }

    @IntRange(from = 0, to = ((int) Short.MAX_VALUE) * 2)
    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    public int readInt() {
        return ((readUnsignedByte() << 24) | (readUnsignedByte() << 16) | (readUnsignedByte() << 8) | (readUnsignedByte()));
    }

    public byte[] readBytes(int count) {
        if (count > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        byte[] ret = new byte[count];
        System.arraycopy(this.bytes, this.position, ret, 0, count);
        this.position += count;
        return ret;
    }

    public long readLong() {
        return (((long) readUnsignedByte() << 56) | ((long) readUnsignedByte() << 48) | ((long) readUnsignedByte() << 40) | ((long) readUnsignedByte() << 32) | ((long) readUnsignedByte() << 24) | (readUnsignedByte() << 16) | (readUnsignedByte() << 8) | (readUnsignedByte()));
    }

    public double readFixedPointNumberInt() {
        return readInt() / 32.0D;
    }

    public String readString() {
        String string = new String(readBytes(readVarInt()), StandardCharsets.UTF_8);
        if (string.length() > ProtocolDefinition.STRING_MAX_LEN) {
            throw new IllegalArgumentException(String.format("String max string length exceeded %d > %d", string.length(), ProtocolDefinition.STRING_MAX_LEN));
        }
        return string;
    }

    public long readVarLong() {
        int byteCount = 0;
        long result = 0;
        byte read;
        do {
            read = readByte();
            result |= (long) (read & 0x7F) << (7 * byteCount);
            byteCount++;
            if (byteCount > 10) {
                throw new IllegalArgumentException("VarLong is too big");
            }
        } while ((read & 0x80) != 0);
        return result;
    }

    public boolean readBoolean() {
        return readByte() == 1;
    }

    public int[] readUnsignedShortsLE(int count) {
        if (count > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        int[] ret = new int[count];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = ((readUnsignedByte()) | (readUnsignedByte() << 8));
        }
        return ret;
    }

    public String[] readStringArray(int length) {
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        String[] ret = new String[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readString();
        }
        return ret;
    }

    public String[] readStringArray() {
        return readStringArray(readVarInt());
    }

    public String readString(int length) {
        return new String(readBytes(length));
    }

    public UUID readUUID() {
        return new UUID(readLong(), readLong());
    }

    public int readVarInt() {
        int byteCount = 0;
        int result = 0;
        byte read;
        do {
            read = readByte();
            result |= (read & 0x7F) << (7 * byteCount);
            byteCount++;
            if (byteCount > 5) {
                throw new IllegalArgumentException("VarInt is too big");
            }
        } while ((read & 0x80) != 0);

        return result;
    }

    public double readFixedPointNumberByte() {
        return readByte() / 32.0D;
    }

    public JsonObject readJSON() {
        return JsonParser.parseString(readString()).getAsJsonObject();
    }

    public byte readByte() {
        return this.bytes[this.position++];
    }

    @IntRange(from = 0, to = ((int) Byte.MAX_VALUE) * 2 + 1)
    public int readUnsignedByte() {
        return (this.bytes[this.position++] & 0xFF);
    }

    public ChatComponent readChatComponent() {
        return ChatComponent.Companion.valueOf(null, null, readString());
    }

    @IntRange(from = 0)
    public int getLength() {
        return this.bytes.length;
    }

    public Directions readDirection() {
        return Directions.byId(readVarInt());
    }

    public Poses readPose() {
        return Poses.byId(readVarInt());
    }

    public NBTTag readNBT(boolean compressed) {
        if (compressed) {
            int length = readShort();
            if (length == -1) {
                // no nbt data here...
                return new CompoundTag();
            }
            try {
                return new InByteBuffer(Util.decompressGzip(readBytes(length)), this.connection).readNBT();
            } catch (IOException e) {
                // oh no
                e.printStackTrace();
                throw new IllegalArgumentException("Bad nbt");
            }
        }
        TagTypes type = TagTypes.byId(readUnsignedByte());
        if (type == TagTypes.COMPOUND) {
            // shouldn't be a subtag
            return new CompoundTag(false, this);
        }
        return readNBT(type);
    }

    public NBTTag readNBT(TagTypes tagType) {
        return switch (tagType) {
            case END -> null;
            case BYTE -> new ByteTag(this);
            case SHORT -> new ShortTag(this);
            case INT -> new IntTag(this);
            case LONG -> new LongTag(this);
            case FLOAT -> new FloatTag(this);
            case DOUBLE -> new DoubleTag(this);
            case BYTE_ARRAY -> new ByteArrayTag(this);
            case STRING -> new StringTag(this);
            case LIST -> new ListTag(this);
            case COMPOUND -> new CompoundTag(true, this);
            case INT_ARRAY -> new IntArrayTag(this);
            case LONG_ARRAY -> new LongArrayTag(this);
        };
    }

    public NBTTag readNBT() {
        return readNBT(false);
    }

    public String getBase64() {
        return getBase64(getPosition(), getBytesLeft());
    }

    public String getBase64(int pos, int length) {
        return new String(Base64.getEncoder().encode(readBytes(pos, length)));
    }

    @IntRange(from = 0, to = ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE)
    public int getPosition() {
        return this.position;
    }

    public void setPosition(int pos) {
        this.position = pos;
    }

    @IntRange(from = 0)
    public int getBytesLeft() {
        return this.bytes.length - this.position;
    }

    byte[] readBytes(int pos, int count) {
        if (count > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        byte[] ret = new byte[count];
        System.arraycopy(this.bytes, pos, ret, 0, count);
        return ret;
    }

    public short readAngle() {
        return (short) (readByte() * ProtocolDefinition.ANGLE_CALCULATION_CONSTANT);
    }

    public Vec3 readEntityPosition() {
        return new Vec3(readDouble(), readDouble(), readDouble());
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public Vec3 readFloatPosition() {
        return new Vec3(readFloat(), readFloat(), readFloat());
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public Vec3i readBlockPositionByte() {
        return new Vec3i(readInt(), readUnsignedByte(), readInt());
    }

    public Vec3i readBlockPositionShort() {
        return new Vec3i(readInt(), readShort(), readInt());
    }

    public Vec3i readBlockPositionInteger() {
        return new Vec3i(readInt(), readInt(), readInt());
    }

    public byte[] readBytesLeft() {
        return readBytes(getBytesLeft());
    }

    public int[] readIntArray(int length) {
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        int[] ret = new int[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readInt();
        }
        return ret;
    }


    public long[] readLongArray(int length) {
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        long[] ret = new long[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readLong();
        }
        return ret;
    }

    public long[] readLongArray() {
        return readLongArray(readVarInt());
    }

    @Override
    public String toString() {
        return "dataLen: " + this.bytes.length + "; position: " + this.position;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public int[] readVarIntArray(int length) {
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        int[] ret = new int[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readVarInt();
        }
        return ret;
    }

    public int[] readVarIntArray() {
        return readVarIntArray(readVarInt());
    }


    public Connection getConnection() {
        return this.connection;
    }


    public CommandNode[] readCommandNodesArray() {
        int count = readVarInt();

        if (count > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        CommandNode[] nodes = new CommandNode[count];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = readCommandNode();
        }
        // resole ids
        for (CommandNode node : nodes) {
            // redirect
            if (node.getRedirectNodeId() != -1) {
                node.setRedirectNode(nodes[node.getRedirectNodeId()]);
            }

            // children
            for (int id : node.getChildrenIds()) {
                CommandNode targetNode = nodes[id];
                if (targetNode instanceof CommandArgumentNode argumentNode) {
                    node.getArgumentsChildren().add(argumentNode);
                } else if (targetNode instanceof CommandLiteralNode literalNode) {
                    node.getLiteralChildren().put(literalNode.getName(), literalNode);
                }
            }
            node.resetChildrenIds();

        }
        return nodes;
    }

    private CommandNode readCommandNode() {
        byte flags = readByte();
        return switch (CommandNode.NodeTypes.byId(flags & 0x03)) {
            case ROOT -> new CommandRootNode(flags, this);
            case LITERAL -> new CommandLiteralNode(flags, this);
            case ARGUMENT -> new CommandArgumentNode(flags, this);
        };
    }

    public ResourceLocation readResourceLocation() {
        String resourceLocation = readString();

        if (Util.doesStringContainsUppercaseLetters(resourceLocation)) {
            // just a string but wrapped into a resourceLocation (like old plugin channels MC|BRAND or ...)
            return new LegacyResourceLocation(resourceLocation);
        }
        return new ResourceLocation(resourceLocation);
    }

    public Vec2i readChunkPosition() {
        return new Vec2i(readInt(), readInt());
    }

    public long[] readVarLongArray(int size) {
        var ret = new long[size];
        for (int i = 0; i < size; i++) {
            ret[i] = readVarLong();
        }
        return ret;
    }

    public long[] readVarLongArray() {
        return readVarLongArray(readVarInt());
    }
}
