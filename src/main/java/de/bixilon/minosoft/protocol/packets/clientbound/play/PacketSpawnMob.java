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

import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.game.datatypes.entities.Mob;
import de.bixilon.minosoft.game.datatypes.entities.Mobs;
import de.bixilon.minosoft.game.datatypes.entities.Velocity;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.lang.reflect.InvocationTargetException;

public class PacketSpawnMob implements ClientboundPacket {
    Mob mob;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                int entityId = buffer.readVarInt();
                Mobs type = Mobs.byType(buffer.readByte());
                Location location = new Location(buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger());
                int yaw = buffer.readByte();
                int pitch = buffer.readByte();
                int headPitch = buffer.readByte();
                Velocity velocity = new Velocity(buffer.readShort(), buffer.readShort(), buffer.readShort());

                assert type != null;
                try {
                    mob = (Mob) type.getClazz().getConstructor(int.class, Location.class, int.class, int.class, Velocity.class, InByteBuffer.class, ProtocolVersion.class).newInstance(entityId, location, yaw, pitch, velocity, buffer, v);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Mob spawned (entityId=%d, type=%s, at %s %s %s)", mob.getId(), mob.getEntityType().name(), mob.getLocation().getX(), mob.getLocation().getY(), mob.getLocation().getZ()));
    }

    public Mob getMob() {
        return mob;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
