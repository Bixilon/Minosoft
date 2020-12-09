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

import de.bixilon.minosoft.data.PlayerPropertyData;
import de.bixilon.minosoft.data.entities.EntityMetaData;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.Velocity;
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity;
import de.bixilon.minosoft.data.mappings.Item;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.util.HashSet;
import java.util.UUID;

public class PacketSpawnPlayer implements ClientboundPacket {
    PlayerEntity entity;
    Velocity velocity;

    @Override
    public boolean read(InByteBuffer buffer) {
        int entityId = buffer.readVarInt();
        String name = null;
        UUID uuid;
        HashSet<PlayerPropertyData> properties = null;
        if (buffer.getVersionId() < 19) {
            name = buffer.readString();
            uuid = UUID.fromString(buffer.readString());
            properties = new HashSet<>();
            int length = buffer.readVarInt();
            for (int i = 0; i < length; i++) {
                properties.add(new PlayerPropertyData(buffer.readString(), buffer.readString(), buffer.readString()));
            }
        } else {
            uuid = buffer.readUUID();
        }
        Location location;
        if (buffer.getVersionId() < 100) {
            location = new Location(buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt());
        } else {
            location = buffer.readLocation();
        }
        short yaw = buffer.readAngle();
        short pitch = buffer.readAngle();

        Item currentItem = null;
        if (buffer.getVersionId() < 49) {
            currentItem = buffer.getConnection().getMapping().getItemById(buffer.readUnsignedShort());
        }
        EntityMetaData metaData = null;
        if (buffer.getVersionId() < 550) {
            metaData = buffer.readMetaData();
        }
        this.entity = new PlayerEntity(buffer.getConnection(), entityId, uuid, location, new EntityRotation(yaw, pitch, 0), name, properties, currentItem);
        if (metaData != null) {
            entity.setMetaData(metaData);
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Player spawned at %s (entityId=%d, name=%s, uuid=%s)", entity.getLocation(), entity.getEntityId(), entity.getName(), entity.getUUID()));
    }

    public PlayerEntity getEntity() {
        return entity;
    }

    public Velocity getVelocity() {
        return velocity;
    }

}
