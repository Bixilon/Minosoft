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
import de.bixilon.minosoft.data.mappings.VersionTweaker;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class PacketSpawnObject implements ClientboundPacket {
    Entity entity;
    Velocity velocity;

    @Override
    public boolean read(InByteBuffer buffer) throws Exception {
        int entityId = buffer.readVarInt();
        UUID uuid = null;
        if (buffer.getVersionId() >= 49) {
            uuid = buffer.readUUID();
        }

        int type;
        if (buffer.getVersionId() < 301) {
            type = buffer.readByte();
        } else {
            type = buffer.readVarInt();
        }
        Class<? extends Entity> typeClass;
        if (buffer.getVersionId() < 458) {
            typeClass = Objects.byId(type).getClazz();
        } else {
            typeClass = buffer.getConnection().getMapping().getEntityClassById(type);
        }

        Location location;
        if (buffer.getVersionId() < 100) {
            location = new Location(buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger());
        } else {
            location = buffer.readLocation();
        }
        EntityRotation rotation = new EntityRotation(buffer.readAngle(), buffer.readAngle(), 0);
        int data = buffer.readInt();

        if (buffer.getVersionId() < 49) {
            if (data != 0) {
                velocity = new Velocity(buffer.readShort(), buffer.readShort(), buffer.readShort());
            }
        } else {
            velocity = new Velocity(buffer.readShort(), buffer.readShort(), buffer.readShort());
        }

        if (buffer.getVersionId() <= 47) { // ToDo
            typeClass = VersionTweaker.getRealEntityObjectClass(typeClass, data, buffer.getVersionId());
        }

        if (typeClass == null) {
            throw new UnknownEntityException(String.format("Unknown entity (typeId=%d)", type));
        }

        try {
            entity = typeClass.getConstructor(Connection.class, int.class, UUID.class, Location.class, EntityRotation.class).newInstance(buffer.getConnection(), entityId, uuid, location, rotation);
            return true;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Object spawned at %s (entityId=%d, type=%s)", entity.getLocation().toString(), entity.getEntityId(), entity));
    }

    public Entity getEntity() {
        return entity;
    }

    public Velocity getVelocity() {
        return velocity;
    }

}
