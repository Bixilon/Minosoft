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
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.Velocity;
import de.bixilon.minosoft.data.entities.meta.HumanMetaData;
import de.bixilon.minosoft.data.entities.mob.OtherPlayer;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.util.UUID;

public class PacketSpawnPlayer implements ClientboundPacket {
    OtherPlayer entity;
    Velocity velocity;

    @Override
    public boolean read(InByteBuffer buffer) {
        int entityId = buffer.readVarInt();
        String name = null;
        UUID uuid;
        PlayerPropertyData[] properties = null;
        if (buffer.getVersionId() < 19) {
            name = buffer.readString();
            uuid = UUID.fromString(buffer.readString());
            properties = new PlayerPropertyData[buffer.readVarInt()];
            for (int i = 0; i < properties.length; i++) {
                properties[i] = new PlayerPropertyData(buffer.readString(), buffer.readString(), buffer.readString());
            }
        } else {
            uuid = buffer.readUUID();
        }
        Location location;
        if (buffer.getVersionId() < 100) {
            location = new Location(buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger());
        } else {
            location = buffer.readLocation();
        }
        short yaw = buffer.readAngle();
        short pitch = buffer.readAngle();

        short currentItem = 0;
        if (buffer.getVersionId() < 49) {
            currentItem = buffer.readShort();
        }
        HumanMetaData metaData = null;
        if (buffer.getVersionId() < 550) {
            metaData = new HumanMetaData(buffer.readMetaData(), buffer.getVersionId());
        }
        this.entity = new OtherPlayer(entityId, name, uuid, properties, location, yaw, pitch, 0, currentItem, metaData);
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Player spawned at %s (entityId=%d, name=%s, uuid=%s)", entity.getLocation().toString(), entity.getEntityId(), entity.getName(), entity.getUUID()));
    }

    public OtherPlayer getEntity() {
        return entity;
    }

    public Velocity getVelocity() {
        return velocity;
    }

}
