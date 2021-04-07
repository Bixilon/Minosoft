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

import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.data.Gamemodes;
import de.bixilon.minosoft.data.PlayerPropertyData;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity;
import de.bixilon.minosoft.data.entities.meta.EntityMetaData;
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;

import java.util.HashSet;
import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketSpawnPlayer extends PlayClientboundPacket {
    private final int entityId;
    private final UUID entityUUID;
    private final PlayerEntity entity;

    public PacketSpawnPlayer(PlayInByteBuffer buffer) {
        this.entityId = buffer.readVarInt();
        String name = "TBA";
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
            position = buffer.readEntityPosition();
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
        this.entity = new PlayerEntity(buffer.getConnection(), buffer.getConnection().getMapping().getEntityRegistry().get(PlayerEntity.Companion.getRESOURCE_LOCATION()), position, new EntityRotation(yaw, pitch, 0), name, this.entityUUID, properties, Gamemodes.CREATIVE); // ToDo
        if (metaData != null) {
            this.entity.setEntityMetaData(metaData);
            if (StaticConfiguration.VERBOSE_ENTITY_META_DATA_LOGGING) {
                Log.verbose(String.format("Metadata of entity %s (entityId=%d): %s", this.entity.toString(), this.entityId, this.entity.getEntityMetaDataAsString()));
            }
        }
    }

    @Override
    public void handle(PlayConnection connection) {
        connection.fireEvent(new EntitySpawnEvent(connection, this));

        connection.getWorld().addEntity(this.entityId, this.entityUUID, this.entity);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Player spawned at %s (entityId=%d, name=%s, uuid=%s)", this.entity.getPosition(), this.entityId, this.entity.getName(), this.entity.getUuid()));
    }

    public PlayerEntity getEntity() {
        return this.entity;
    }

}
