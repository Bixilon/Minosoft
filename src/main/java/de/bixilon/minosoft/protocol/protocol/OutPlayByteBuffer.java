/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.minosoft.data.inventory.ItemStack;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import glm_.vec3.Vec3i;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class OutPlayByteBuffer extends OutByteBuffer {
    private final PlayConnection connection;
    private final int versionId;

    public OutPlayByteBuffer() {
        this.connection = null;
        this.versionId = -1;
    }

    public OutPlayByteBuffer(PlayConnection connection) {
        super(connection);
        this.connection = connection;
        this.versionId = connection.getVersion().getVersionId();
    }

    @SuppressWarnings("unchecked")
    public OutPlayByteBuffer(OutPlayByteBuffer buffer) {
        super(buffer);
        this.connection = buffer.getConnection();
        this.versionId = buffer.getVersionId();
    }

    public void writeByteArray(byte[] data) {
        if (this.versionId < V_14W21A) {
            writeShort((short) data.length);
        } else {
            writeVarInt(data.length);
        }
        writeBytes(data);
    }

    public void writePosition(Vec3i position) {
        if (position == null) {
            writeLong(0L);
            return;
        }
        if (this.versionId < V_18W43A) {
            writeLong((((long) position.getX() & 0x3FFFFFF) << 38) | (((long) position.getZ() & 0x3FFFFFF)) | ((long) position.getY() & 0xFFF) << 26);
            return;
        }
        writeLong((((long) (position.getX() & 0x3FFFFFF) << 38) | ((long) (position.getZ() & 0x3FFFFFF) << 12) | (long) (position.getY() & 0xFFF)));
    }

    public void writeItemStack(ItemStack itemStack) {
        if (this.versionId < V_1_13_2_PRE1) {
            if (itemStack == null) {
                writeShort((short) -1);
                return;
            }
            writeShort((short) this.connection.getMapping().getItemRegistry().getId(itemStack.getItem()));
            writeByte((byte) itemStack.getItemCount());
            writeShort((short) itemStack.getItemMetadata());
            writeNBT(itemStack.getNBT());
        }
        if (itemStack == null) {
            writeBoolean(false);
            return;
        }
        writeVarInt(this.connection.getMapping().getItemRegistry().getId(itemStack.getItem()));
        writeByte((byte) itemStack.getItemCount());
        writeNBT(itemStack.getNBT());
    }

    public void writeEntityId(int entityId) {
        if (this.versionId < V_14W04A) {
            writeInt(entityId);
        } else {
            writeVarInt(entityId);
        }
    }

    public PlayConnection getConnection() {
        return this.connection;
    }

    public int getVersionId() {
        return this.versionId;
    }
}
