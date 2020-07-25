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

import de.bixilon.minosoft.game.datatypes.entities.Entity;
import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.game.datatypes.entities.Velocity;
import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;
import de.bixilon.minosoft.game.datatypes.objectLoader.entities.Entities;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.UUID;

public class PacketSpawnMob implements ClientboundPacket {
    Entity entity;

    @Override
    public boolean read(InByteBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
            case VERSION_1_8: {
                int entityId = buffer.readVarInt();
                Class<? extends Entity> type = Entities.byId(buffer.readByte(), buffer.getVersion());
                Location location = new Location(buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger());
                short yaw = buffer.readAngle();
                short pitch = buffer.readAngle();
                int headYaw = buffer.readAngle();
                Velocity velocity = new Velocity(buffer.readShort(), buffer.readShort(), buffer.readShort());

                assert type != null;
                try {
                    entity = type.getConstructor(int.class, Location.class, short.class, short.class, Velocity.class, HashMap.class, ProtocolVersion.class).newInstance(entityId, location, yaw, pitch, velocity, buffer.readMetaData(), buffer.getVersion());
                    return true;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            return false;
            case VERSION_1_9_4:
            case VERSION_1_10: {
                int entityId = buffer.readVarInt();
                UUID uuid = buffer.readUUID();
                Class<? extends Entity> type = Entities.byId(buffer.readByte(), buffer.getVersion());
                Location location = new Location(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
                short yaw = buffer.readAngle();
                short pitch = buffer.readAngle();
                int headYaw = buffer.readAngle();
                Velocity velocity = new Velocity(buffer.readShort(), buffer.readShort(), buffer.readShort());

                assert type != null;
                try {
                    entity = type.getConstructor(int.class, Location.class, short.class, short.class, Velocity.class, HashMap.class, ProtocolVersion.class).newInstance(entityId, location, yaw, pitch, velocity, buffer.readMetaData(), buffer.getVersion());
                    return true;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NullPointerException e) {
                    e.printStackTrace();
                }
                return false;
            }
            default: {
                int entityId = buffer.readVarInt();
                UUID uuid = buffer.readUUID();
                Class<? extends Entity> type = Entities.byId(buffer.readVarInt(), buffer.getVersion());
                Location location = new Location(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
                short yaw = buffer.readAngle();
                short pitch = buffer.readAngle();
                int headYaw = buffer.readAngle();
                Velocity velocity = new Velocity(buffer.readShort(), buffer.readShort(), buffer.readShort());


                HashMap<Integer, EntityMetaData.MetaDataSet> mataData = null;
                if (buffer.getVersion().getVersionNumber() < ProtocolVersion.VERSION_1_15_2.getVersionNumber()) {
                    mataData = buffer.readMetaData();
                }
                try {
                    entity = type.getConstructor(int.class, Location.class, short.class, short.class, Velocity.class, HashMap.class, ProtocolVersion.class).newInstance(entityId, location, yaw, pitch, velocity, mataData, buffer.getVersion());
                    return true;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NullPointerException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Mob spawned at %s (entityId=%d, type=%s)", entity.getLocation().toString(), entity.getEntityId(), entity.getIdentifier()));
    }

    public Entity getMob() {
        return entity;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
