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

import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.mappings.DefaultRegistries;
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;

import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketSpawnObject extends PlayS2CPacket {
    private final int entityId;
    private UUID entityUUID;
    private final Entity entity;
    private Vec3 velocity;

    public PacketSpawnObject(PlayInByteBuffer buffer) throws Exception {
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
        EntityRotation rotation = new EntityRotation(buffer.readAngle(), buffer.readAngle(), 0);
        int data = buffer.readInt();

        if (buffer.getVersionId() < V_15W31A) {
            if (data != 0) {
                this.velocity = new Vec3(buffer.readShort(), buffer.readShort(), buffer.readShort()).times(ProtocolDefinition.VELOCITY_CONSTANT);
            }
        } else {
            this.velocity = new Vec3(buffer.readShort(), buffer.readShort(), buffer.readShort()).times(ProtocolDefinition.VELOCITY_CONSTANT);
        }

        if (buffer.getVersionId() < V_19W05A) {
            var entityResourceLocation = DefaultRegistries.INSTANCE.getENTITY_OBJECT_REGISTRY().get(type).getResourceLocation();
            this.entity = buffer.getConnection().getMapping().getEntityRegistry().get(entityResourceLocation).build(buffer.getConnection(), position, rotation, null, buffer.getVersionId()); // ToDo: Entity meta data tweaking
        } else {
            this.entity = buffer.getConnection().getMapping().getEntityRegistry().get(type).build(buffer.getConnection(), position, rotation, null, buffer.getVersionId());
        }
    }

    @Override
    public void handle(PlayConnection connection) {
        connection.fireEvent(new EntitySpawnEvent(connection, this));

        connection.getWorld().getEntities().add(this.entityId, this.entityUUID, getEntity());
        this.entity.setVelocity(this.velocity);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Object spawned at %s (entityId=%d, type=%s)", this.entity.getPosition(), this.entityId, this.entity));
    }

    public Entity getEntity() {
        return this.entity;
    }
}
