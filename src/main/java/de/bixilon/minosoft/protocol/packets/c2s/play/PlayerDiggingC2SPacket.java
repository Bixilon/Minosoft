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

package de.bixilon.minosoft.protocol.packets.c2s.play;

import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W31A;

public class PlayerDiggingC2SPacket implements PlayC2SPacket {
    private final DiggingStatus status;
    private final Vec3i position;
    private final DiggingFaces face;

    public PlayerDiggingC2SPacket(DiggingStatus status, Vec3i position, DiggingFaces face) {
        this.status = status;
        this.position = position;
        this.face = face;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        if (buffer.getVersionId() < V_15W31A) { // ToDo
            buffer.writeByte((byte) this.status.ordinal());
        } else {
            buffer.writeVarInt(this.status.ordinal());
        }

        if (buffer.getVersionId() < V_14W04A) {
            if (this.position == null) {
                buffer.writeInt(0);
                buffer.writeByte((byte) 0);
                buffer.writeInt(0);
            } else {
                buffer.writeVec3iByte(this.position);
            }
        } else {
            buffer.writePosition(this.position);
        }

        buffer.writeByte(this.face.getId());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Send player digging packet (status=%s, position=%s, face=%s)", this.status, this.position, this.face));
    }

    public enum DiggingStatus {
        START_DIGGING,
        CANCELLED_DIGGING,
        FINISHED_DIGGING,
        DROP_ITEM_STACK,
        DROP_ITEM,
        SHOOT_ARROW__FINISH_EATING,
        SWAP_ITEMS_IN_HAND;

        private static final DiggingStatus[] DIGGING_STATUSES = values();

        public static DiggingStatus byId(int id) {
            return DIGGING_STATUSES[id];
        }
    }

    public enum DiggingFaces {
        BOTTOM(0),
        TOP(1),
        NORTH(2),
        SOUTH(3),
        WEST(4),
        EAST(5),
        SPECIAL(255);

        private final byte id;

        DiggingFaces(int id) {
            this.id = (byte) id;
        }

        public byte getId() {
            return this.id;
        }
    }
}
