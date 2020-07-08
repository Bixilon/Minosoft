/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.entities.EntityObject;
import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.game.datatypes.entities.Objects;
import de.bixilon.minosoft.game.datatypes.entities.Velocity;
import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class PacketSpawnObject implements ClientboundPacket {
    EntityObject object;

    public static EntityMetaData getEntityData(Class<? extends EntityMetaData> clazz, InByteBuffer buffer, ProtocolVersion v) {
        try {
            return clazz.getConstructor(InByteBuffer.class, ProtocolVersion.class).newInstance(buffer, v);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Object spawned at %s (entityId=%d, type=%s)", object.getLocation().toString(), object.getEntityId(), object.getEntityType().name()));
    }

    public EntityObject getObject() {
        return object;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
            case VERSION_1_8: {
                int entityId = buffer.readVarInt();
                Objects type = Objects.byType(buffer.readByte());
                Location location = new Location(buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger());
                short pitch = buffer.readAngle();
                short yaw = buffer.readAngle();
                int data = buffer.readInt();

                try {
                    if (buffer.getVersion().getVersionNumber() >= ProtocolVersion.VERSION_1_8.getVersionNumber()) {
                        // velocity present AND metadata

                        Velocity velocity = null;
                        if (data != 0) {
                            velocity = new Velocity(buffer.readShort(), buffer.readShort(), buffer.readShort());
                        }
                        object = type.getClazz().getConstructor(int.class, Location.class, short.class, short.class, int.class, Velocity.class).newInstance(entityId, location, yaw, pitch, data, velocity);
                        return true;
                    } else {
                        object = type.getClazz().getConstructor(int.class, Location.class, short.class, short.class, int.class).newInstance(entityId, location, yaw, pitch, data);
                        return true;
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2: {
                int entityId = buffer.readVarInt();
                UUID uuid = buffer.readUUID();
                Objects type = Objects.byType(buffer.readByte());
                Location location = new Location(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
                short pitch = buffer.readAngle();
                short yaw = buffer.readAngle();
                int data = buffer.readInt();

                try {
                    // velocity present AND metadata
                    Velocity velocity = new Velocity(buffer.readShort(), buffer.readShort(), buffer.readShort());
                    object = type.getClazz().getConstructor(int.class, Location.class, short.class, short.class, int.class, Velocity.class).newInstance(entityId, location, yaw, pitch, data, velocity);
                    return true;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
