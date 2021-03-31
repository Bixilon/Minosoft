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

import de.bixilon.minosoft.data.Gamemodes;
import de.bixilon.minosoft.data.PlayerPropertyData;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity;
import de.bixilon.minosoft.data.entities.meta.EntityMetaData;
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;

import java.util.HashSet;
import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketSpawnPlayer extends ClientboundPacket {
    private final int entityId;
    private final UUID entityUUID;
    private final PlayerEntity entity;

    public PacketSpawnPlayer(InByteBuffer buffer) {
        this.entityId = buffer.readVarInt();
        String name = null;
        HashSet<PlayerPropertyData> properties = null;
        if (buffer.getVersionId() < V_14W21A) {
            name = buffer.readString();
            this.entityUUID = UUID.fromString(buffer.readString());
            properties = new HashSet<>();
            int length = buffer.readVarInt();
            for (int i = 0; i < length; i++) {
                properties.add(new PlayerPropertyData(buffer.readString(), buffer.readString(), buffer.readString()));
            }
        } else {
            this.entityUUID = buffer.readUUID();
        }
        Vec3 position;
        if (buffer.getVersionId() < V_16W06A) {
            position = new Vec3(buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt());
        } else {
            position = buffer.readLocation();
        }
        short yaw = buffer.readAngle();
        short pitch = buffer.readAngle();

        if (buffer.getVersionId() < V_15W31A) {
            buffer.getConnection().getMapping().getItemRegistry().get(buffer.readUnsignedShort()); // current item
        }
        EntityMetaData metaData = null;
        if (buffer.getVersionId() < V_19W34A) {
            metaData = buffer.readMetaData();
        }
        this.entity = new PlayerEntity(buffer.getConnection(), position, new EntityRotation(yaw, pitch, 0), name, properties, Gamemodes.CREATIVE); // ToDo
        if (metaData != null) {
            this.entity.setMetaData(metaData);
        }
    }

    @Override
    public void handle(Connection connection) {
        connection.fireEvent(new EntitySpawnEvent(connection, this));

        connection.getWorld().addEntity(this.entityId, this.entityUUID, this.entity);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Player spawned at %s (entityId=%d, name=%s, uuid=%s)", this.entity.getPosition(), this.entityId, this.entity.getName(), this.entity.getUUID()));
    }

    public PlayerEntity getEntity() {
        return this.entity;
    }

}
