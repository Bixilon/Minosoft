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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.game.datatypes.entities.Entity;
import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.game.datatypes.player.Hand;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

public class PacketInteractEntity implements ServerboundPacket {
    final int entityId;
    Location location;
    Hand hand;
    Click click;
    boolean sneaking;

    public PacketInteractEntity(Entity entity, Click click) {
        this.entityId = entity.getEntityId();
        this.click = click;
    }

    public PacketInteractEntity(int entityId, Click click) {
        this.entityId = entityId;
        this.click = click;
    }

    public PacketInteractEntity(int entityId, Click click, Location location) {
        this.entityId = entityId;
        this.click = click;
        this.location = location;
    }

    public PacketInteractEntity(int entityId, Click click, Location location, Hand hand) {
        this.entityId = entityId;
        this.click = click;
        this.location = location;
        this.hand = hand;
    }

    public PacketInteractEntity(int entityId, Click click, Location location, Hand hand, boolean sneaking) {
        this.entityId = entityId;
        this.click = click;
        this.location = location;
        this.hand = hand;
        this.sneaking = sneaking;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_INTERACT_ENTITY);
        buffer.writeEntityId(entityId);
        if (buffer.getProtocolId() < 33) {
            if (click == Click.INTERACT_AT) {
                click = Click.INTERACT;
            }
        }
        buffer.writeByte((byte) click.getId());
        if (buffer.getProtocolId() >= 33) {
            if (click == Click.INTERACT_AT) {
                // position
                buffer.writeFloat((float) location.getX());
                buffer.writeFloat((float) location.getY());
                buffer.writeFloat((float) location.getZ());
            }

            if (click == Click.INTERACT_AT || click == Click.INTERACT) {
                if (buffer.getProtocolId() >= 49) {
                    buffer.writeVarInt(hand.getId());
                }
            }
        }
        if (buffer.getProtocolId() >= 743) { //ToDo
            buffer.writeBoolean(sneaking);
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Interacting with entity (entityId=%d, click=%s)", entityId, click));
    }

    public enum Click {
        INTERACT(0),
        ATTACK(1),
        INTERACT_AT(2);

        final int id;

        Click(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
