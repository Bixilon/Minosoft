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

package de.bixilon.minosoft.protocol.packets.s2c.play;

import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.entities.entities.UnknownEntityException;
import de.bixilon.minosoft.data.entities.meta.EntityMetaData;
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;

import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketSpawnMob extends PlayS2CPacket {
    private final int entityId;
    private final Vec3 velocity;
    private final Entity entity;
    private UUID entityUUID;

    public PacketSpawnMob(PlayInByteBuffer buffer) throws Exception {
        this.entityId = buffer.readEntityId();
        if (buffer.getVersionId() >= V_15W31A) {
            this.entityUUID = buffer.readUUID();
        }
        int type;
        if (buffer.getVersionId() < V_16W32A) {
            type = buffer.readByte();
        } else {
            type = buffer.readVarInt();
        }

        Vec3 position;
        if (buffer.getVersionId() < V_16W06A) {
            position = new Vec3(buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt());
        } else {
            position = buffer.readPosition();
        }
        EntityRotation rotation = new EntityRotation(buffer.readAngle(), buffer.readAngle(), buffer.readAngle());
        this.velocity = new Vec3(buffer.readShort(), buffer.readShort(), buffer.readShort()).times(ProtocolDefinition.VELOCITY_CONSTANT);

        EntityMetaData metaData = null;
        if (buffer.getVersionId() < V_19W34A) {
            metaData = buffer.readMetaData();
            // we have meta data, check if we need to correct the class
        }

        final var entityType = buffer.getConnection().getMapping().getEntityRegistry().get(type);

        if (entityType == null) {
            throw new UnknownEntityException(String.format("Unknown entity (typeId=%d)", type));
        }

        this.entity = entityType.build(buffer.getConnection(), position, rotation, metaData, buffer.getVersionId());

        if (metaData != null) {
            this.entity.setEntityMetaData(metaData);
            if (StaticConfiguration.VERBOSE_ENTITY_META_DATA_LOGGING) {
                Log.verbose(String.format("Metadata of entity %s (entityId=%d): %s", this.entity, this.entityId, this.entity.getEntityMetaDataAsString()));
            }
        }
    }

    @Override
    public void handle(PlayConnection connection) {
        connection.fireEvent(new EntitySpawnEvent(connection, this));
        connection.getWorld().addEntity(this.entityId, this.entityUUID, getEntity());
        this.entity.setVelocity(this.velocity);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Mob spawned at %s (entityId=%d, type=%s)", this.entity.getPosition(), this.entityId, this.entity));
    }

    public Entity getEntity() {
        return this.entity;
    }
}
