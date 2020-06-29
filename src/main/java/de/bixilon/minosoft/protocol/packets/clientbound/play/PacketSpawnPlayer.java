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

import de.bixilon.minosoft.game.datatypes.PlayerPropertyData;
import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.game.datatypes.entities.meta.HumanMetaData;
import de.bixilon.minosoft.game.datatypes.entities.mob.OtherPlayer;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.util.UUID;


public class PacketSpawnPlayer implements ClientboundPacket {
    int entityId;
    OtherPlayer player;

    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10: {
                this.entityId = buffer.readVarInt();
                UUID uuid = UUID.fromString(buffer.readString());
                String name = buffer.readString();
                PlayerPropertyData[] properties = new PlayerPropertyData[buffer.readVarInt()];
                for (int i = 0; i < properties.length; i++) {
                    properties[i] = new PlayerPropertyData(buffer.readString(), buffer.readString(), buffer.readString());
                }
                Location location = new Location(buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger());
                short yaw = buffer.readAngle();
                short pitch = buffer.readAngle();

                short currentItem = buffer.readShort();
                HumanMetaData metaData = new HumanMetaData(buffer.readMetaData(), buffer.getVersion());

                this.player = new OtherPlayer(entityId, name, uuid, properties, location, null, yaw, pitch, currentItem, metaData);
                return true;
            }
            case VERSION_1_8: {
                this.entityId = buffer.readVarInt();
                UUID uuid = buffer.readUUID();
                Location location = new Location(buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger());
                short yaw = buffer.readAngle();
                short pitch = buffer.readAngle();

                short currentItem = buffer.readShort();
                HumanMetaData metaData = new HumanMetaData(buffer.readMetaData(), buffer.getVersion());

                this.player = new OtherPlayer(entityId, null, uuid, null, location, null, yaw, pitch, currentItem, metaData);
                return true;
            }
            case VERSION_1_9_4: {
                this.entityId = buffer.readVarInt();
                UUID uuid = buffer.readUUID();
                Location location = new Location(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
                short yaw = buffer.readAngle();
                short pitch = buffer.readAngle();

                HumanMetaData metaData = new HumanMetaData(buffer.readMetaData(), buffer.getVersion());

                this.player = new OtherPlayer(entityId, null, uuid, null, location, null, yaw, pitch, (short) 0, metaData);
                return true;
            }
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Player spawned at %s (name=%s, uuid=%s)", player.getLocation().toString(), player.getName(), player.getUUID()));
    }

    public int getEntityId() {
        return entityId;
    }

    public OtherPlayer getPlayer() {
        return player;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
