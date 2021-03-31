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

import de.bixilon.minosoft.data.entities.entities.LightningBolt;
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent;
import de.bixilon.minosoft.modding.event.events.LightningBoltSpawnEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_16W06A;

public class PacketSpawnWeatherEntity extends ClientboundPacket {
    private final int entityId;
    private final LightningBolt entity;

    public PacketSpawnWeatherEntity(InByteBuffer buffer) {
        this.entityId = buffer.readVarInt();
        byte type = buffer.readByte();
        Vec3 position;
        if (buffer.getVersionId() < V_16W06A) {
            position = new Vec3(buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt());
        } else {
            position = buffer.readLocation();
        }
        this.entity = new LightningBolt(buffer.getConnection(), this.entityId, position);
    }

    @Override
    public void handle(Connection connection) {
        connection.fireEvent(new EntitySpawnEvent(connection, this));
        connection.fireEvent(new LightningBoltSpawnEvent(connection, this));
        connection.getPlayer().getWorld().addEntity(this.entityId, null, this.entity);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Thunderbolt spawned at %s (entityId=%d)", this.entity.getPosition(), this.entityId));
    }

    public LightningBolt getEntity() {
        return this.entity;
    }
}
