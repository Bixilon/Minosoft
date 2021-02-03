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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.player.Hands;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketInteractEntity implements ServerboundPacket {
    private final int entityId;
    Location location;
    Hands hand;
    EntityInteractionClicks click;
    boolean sneaking;

    public PacketInteractEntity(Entity entity, EntityInteractionClicks click) {
        this.entityId = entity.getEntityId();
        this.click = click;
    }

    public PacketInteractEntity(int entityId, EntityInteractionClicks click) {
        this.entityId = entityId;
        this.click = click;
    }

    public PacketInteractEntity(int entityId, EntityInteractionClicks click, Location location) {
        this.entityId = entityId;
        this.click = click;
        this.location = location;
    }

    public PacketInteractEntity(int entityId, EntityInteractionClicks click, Location location, Hands hand) {
        this.entityId = entityId;
        this.click = click;
        this.location = location;
        this.hand = hand;
    }

    public PacketInteractEntity(int entityId, EntityInteractionClicks click, Location location, Hands hand, boolean sneaking) {
        this.entityId = entityId;
        this.click = click;
        this.location = location;
        this.hand = hand;
        this.sneaking = sneaking;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_INTERACT_ENTITY);
        buffer.writeEntityId(this.entityId);
        if (buffer.getVersionId() < V_14W32A) {
            if (this.click == EntityInteractionClicks.INTERACT_AT) {
                this.click = EntityInteractionClicks.INTERACT;
            }
        }
        buffer.writeByte((byte) this.click.ordinal());
        if (buffer.getVersionId() >= V_14W32A) {
            if (this.click == EntityInteractionClicks.INTERACT_AT) {
                // position
                buffer.writeFloat((float) this.location.getX());
                buffer.writeFloat((float) this.location.getY());
                buffer.writeFloat((float) this.location.getZ());
            }

            if (this.click == EntityInteractionClicks.INTERACT_AT || this.click == EntityInteractionClicks.INTERACT) {
                if (buffer.getVersionId() >= V_15W31A) {
                    buffer.writeVarInt(this.hand.ordinal());
                }

                if (buffer.getVersionId() >= V_1_16_PRE3 && buffer.getVersionId() < V_1_16_PRE5) {
                    buffer.writeBoolean(this.sneaking);
                }
            }
            if (buffer.getVersionId() <= V_1_16_PRE5) {
                buffer.writeBoolean(this.sneaking);
            }
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Interacting with entity (entityId=%d, click=%s)", this.entityId, this.click));
    }

    public enum EntityInteractionClicks {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }
}
