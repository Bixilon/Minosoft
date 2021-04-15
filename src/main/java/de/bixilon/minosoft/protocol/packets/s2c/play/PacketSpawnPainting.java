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

import de.bixilon.minosoft.data.Directions;
import de.bixilon.minosoft.data.entities.entities.decoration.Painting;
import de.bixilon.minosoft.data.mappings.Motive;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketSpawnPainting extends PlayS2CPacket {
    private static final ResourceLocation PAINTING_RESOURCE_LOCATION = new ResourceLocation("minecraft:painting");
    private final int entityId;
    private UUID entityUUID;
    private final Painting entity;

    public PacketSpawnPainting(PlayInByteBuffer buffer) {
        this.entityId = buffer.readVarInt();

        if (buffer.getVersionId() >= V_16W02A) {
            this.entityUUID = buffer.readUUID();
        }
        Motive motive;
        if (buffer.getVersionId() < V_18W02A) {
            motive = buffer.getConnection().getMapping().getMotiveRegistry().get(buffer.readResourceLocation());
        } else {
            motive = buffer.getConnection().getMapping().getMotiveRegistry().get(buffer.readVarInt());
        }
        Vec3i position;
        Directions direction;
        if (buffer.getVersionId() < V_14W04B) {
            position = buffer.readBlockPositionInteger();
            direction = Directions.byId(buffer.readInt());
        } else {
            position = buffer.readBlockPosition();
            direction = Directions.byId(buffer.readUnsignedByte());
        }
        this.entity = new Painting(buffer.getConnection(), buffer.getConnection().getMapping().getEntityRegistry().get(PAINTING_RESOURCE_LOCATION), position, direction, motive);
    }

    @Override
    public void handle(PlayConnection connection) {
        connection.fireEvent(new EntitySpawnEvent(connection, this));

        connection.getWorld().addEntity(this.entityId, this.entityUUID, getEntity());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Spawning painting at %s (entityId=%d, motive=%s, direction=%s)", this.entity.getPosition(), this.entityId, this.entity.getMotive(), this.entity.getDirection()));
    }

    public Painting getEntity() {
        return this.entity;
    }
}

