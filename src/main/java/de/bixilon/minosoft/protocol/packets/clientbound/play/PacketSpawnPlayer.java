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
import de.bixilon.minosoft.game.datatypes.entities.OtherPlayer;
import de.bixilon.minosoft.game.datatypes.entities.meta.HumanMetaData;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.UUID;


public class PacketSpawnPlayer implements ClientboundPacket {
    int entityId;
    OtherPlayer player;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                this.entityId = buffer.readVarInt();
                UUID uuid = UUID.fromString(buffer.readString());
                String name = buffer.readString();
                PlayerPropertyData[] properties = new PlayerPropertyData[buffer.readVarInt()];
                for (int i = 0; i < properties.length; i++) {
                    properties[i] = new PlayerPropertyData(buffer.readString(), buffer.readString(), buffer.readString());
                }
                Location location = new Location(buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger(), buffer.readFixedPointNumberInteger());
                int yaw = buffer.readByte();
                int pitch = buffer.readByte();

                short currentItem = buffer.readShort();
                HumanMetaData metaData = new HumanMetaData(buffer, v);

                this.player = new OtherPlayer(entityId, name, uuid, properties, location, null, yaw, pitch, currentItem, metaData);
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Player spawned (name=%s, uuid=%s, location=%s)", player.getName(), player.getUUID(), player.getLocation().toString()));
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
