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

import de.bixilon.minosoft.data.Directions;
import de.bixilon.minosoft.data.entities.entities.decoration.Painting;
import de.bixilon.minosoft.data.mappings.Motive;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.util.UUID;

public class PacketSpawnPainting implements ClientboundPacket {
    Painting entity;

    @Override
    public boolean read(InByteBuffer buffer) {
        int entityId = buffer.readVarInt();
        UUID uuid = null;
        if (buffer.getVersionId() >= 95) {
            uuid = buffer.readUUID();
        }
        Motive motive;
        if (buffer.getVersionId() < 353) {
            motive = buffer.getConnection().getMapping().getMotiveByIdentifier(buffer.readString());
        } else {
            motive = buffer.getConnection().getMapping().getMotiveById(buffer.readVarInt());
        }
        BlockPosition position;
        Directions direction;
        if (buffer.getVersionId() < 8) {
            position = buffer.readBlockPositionInteger();
            direction = Directions.byId(buffer.readInt());
        } else {
            position = buffer.readPosition();
            direction = Directions.byId(buffer.readByte());
        }
        entity = new Painting(buffer.getConnection(), entityId, uuid, position, direction, motive);
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Spawning painting at %s (entityId=%d, motive=%s, direction=%s)", entity.getLocation(), entity.getEntityId(), entity.getMotive(), entity.getDirection()));
    }

    public Painting getEntity() {
        return entity;
    }
}

