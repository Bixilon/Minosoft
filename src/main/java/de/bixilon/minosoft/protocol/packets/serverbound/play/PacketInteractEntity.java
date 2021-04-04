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

import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.player.Hands;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.serverbound.PlayServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketInteractEntity implements PlayServerboundPacket {
    private final int entityId;
    private Vec3 position;
    private Hands hand;
    private EntityInteractionClicks click;
    private boolean sneaking;

    public PacketInteractEntity(PlayConnection connection, Entity entity, EntityInteractionClicks click) {
        this.entityId = connection.getWorld().getEntityIdMap().inverse().get(entity);
        this.click = click;
    }

    public PacketInteractEntity(int entityId, EntityInteractionClicks click, Vec3 position, Hands hand) {
        this.entityId = entityId;
        this.click = click;
        this.position = position;
        this.hand = hand;
    }

    public PacketInteractEntity(int entityId, EntityInteractionClicks click, Vec3 position, Hands hand, boolean sneaking) {
        this.entityId = entityId;
        this.click = click;
        this.position = position;
        this.hand = hand;
        this.sneaking = sneaking;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
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
                buffer.writeFloat(this.position.getX());
                buffer.writeFloat(this.position.getY());
                buffer.writeFloat(this.position.getZ());
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
