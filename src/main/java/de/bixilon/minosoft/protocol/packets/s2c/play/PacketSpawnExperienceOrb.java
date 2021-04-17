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

import de.bixilon.minosoft.data.entities.entities.ExperienceOrb;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_16W06A;

public class PacketSpawnExperienceOrb extends PlayS2CPacket {
    private static final ResourceLocation EXPERIENCE_ORB_RESOURCE_LOCATION = new ResourceLocation("minecraft:experience_orb");
    private final int entityId;
    private final ExperienceOrb entity;

    public PacketSpawnExperienceOrb(PlayInByteBuffer buffer) {
        this.entityId = buffer.readEntityId();
        Vec3 position;
        if (buffer.getVersionId() < V_16W06A) {
            position = new Vec3(buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt());
        } else {
            position = buffer.readPosition();
        }
        int count = buffer.readUnsignedShort();
        this.entity = new ExperienceOrb(buffer.getConnection(), buffer.getConnection().getMapping().getEntityRegistry().get(EXPERIENCE_ORB_RESOURCE_LOCATION), position, count);
    }

    @Override
    public void handle(PlayConnection connection) {
        connection.fireEvent(new EntitySpawnEvent(connection, this));

        connection.getWorld().addEntity(this.entityId, null, getEntity());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Experience orb spawned at %s(entityId=%d, count=%d)", this.entity.getPosition(), this.entityId, this.entity.getCount()));
    }

    public ExperienceOrb getEntity() {
        return this.entity;
    }
}
