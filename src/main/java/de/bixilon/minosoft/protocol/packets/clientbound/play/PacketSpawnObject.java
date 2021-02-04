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

import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.Objects;
import de.bixilon.minosoft.data.entities.Velocity;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.entities.entities.UnknownEntityException;
import de.bixilon.minosoft.data.mappings.tweaker.VersionTweaker;
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketSpawnObject extends ClientboundPacket {
    Entity entity;
    Velocity velocity;

    @Override
    public boolean read(InByteBuffer buffer) throws Exception {
        int entityId = buffer.readEntityId();
        UUID uuid = null;
        if (buffer.getVersionId() >= V_15W31A) {
            uuid = buffer.readUUID();
        }

        int type;
        if (buffer.getVersionId() < V_16W32A) {
            type = buffer.readByte();
        } else {
            type = buffer.readVarInt();
        }
        Class<? extends Entity> typeClass;
        if (buffer.getVersionId() < V_19W05A) {
            typeClass = Objects.byId(type).getClazz();
        } else {
            typeClass = buffer.getConnection().getMapping().getEntityClassById(type);
        }

        Location location;
        if (buffer.getVersionId() < V_16W06A) {
            location = new Location(buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt());
        } else {
            location = buffer.readLocation();
        }
        EntityRotation rotation = new EntityRotation(buffer.readAngle(), buffer.readAngle(), 0);
        int data = buffer.readInt();

        if (buffer.getVersionId() < V_15W31A) {
            if (data != 0) {
                this.velocity = new Velocity(buffer.readShort(), buffer.readShort(), buffer.readShort());
            }
        } else {
            this.velocity = new Velocity(buffer.readShort(), buffer.readShort(), buffer.readShort());
        }

        if (buffer.getVersionId() <= V_1_8_9) { // ToDo
            typeClass = VersionTweaker.getRealEntityObjectClass(typeClass, data, buffer.getVersionId());
        }

        if (typeClass == null) {
            throw new UnknownEntityException(String.format("Unknown entity (typeId=%d)", type));
        }

        try {
            this.entity = typeClass.getConstructor(Connection.class, int.class, UUID.class, Location.class, EntityRotation.class).newInstance(buffer.getConnection(), entityId, uuid, location, rotation);
            return true;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void handle(Connection connection) {
        connection.fireEvent(new EntitySpawnEvent(connection, this));

        connection.getPlayer().getWorld().addEntity(getEntity());
        connection.getVelocityHandler().handleVelocity(getEntity(), getVelocity());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Object spawned at %s (entityId=%d, type=%s)", this.entity.getLocation().toString(), this.entity.getEntityId(), this.entity));
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Velocity getVelocity() {
        return this.velocity;
    }

}
