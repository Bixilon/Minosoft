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

import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;

public class PacketFacePlayer extends ClientboundPacket {
    private final PlayerFaces face;
    private final Vec3 position;
    private int entityId = -1;
    private PlayerFaces entityFace;

    public PacketFacePlayer(InByteBuffer buffer) {
        this.face = PlayerFaces.byId(buffer.readVarInt());
        this.position = buffer.readEntityPosition();
        if (buffer.readBoolean()) {
            // entity present
            this.entityId = buffer.readVarInt();
            this.entityFace = PlayerFaces.byId(buffer.readVarInt());
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received face player packet (face=%s, position=%s, entityId=%d, entityFace=%s)", this.face, this.position, this.entityId, this.entityFace));
    }

    public PlayerFaces getFace() {
        return this.face;
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public PlayerFaces getEntityFace() {
        return this.entityFace;
    }

    public enum PlayerFaces {
        FEET,
        EYES;

        private static final PlayerFaces[] PLAYER_FACES = values();

        public static PlayerFaces byId(int id) {
            return PLAYER_FACES[id];
        }
    }
}
