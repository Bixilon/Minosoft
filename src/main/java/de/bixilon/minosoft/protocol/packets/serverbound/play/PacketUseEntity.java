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
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketUseEntity implements ServerboundPacket {
    final int entityId;
    final Click click;
    final Location location;

    final Hand hand;

    public PacketUseEntity(Entity entity, Click click) {
        this.entityId = entity.getId();
        this.click = click;
        location = null;
        hand = Hand.RIGHT;
        log();
    }

    public PacketUseEntity(int entityId, Click click) {
        this.entityId = entityId;
        this.click = click;
        location = null;
        hand = Hand.RIGHT;
        log();
    }

    public PacketUseEntity(int entityId, Click click, Location location) {
        this.entityId = entityId;
        this.click = click;
        this.location = location;
        hand = Hand.RIGHT;
        log();
    }

    public PacketUseEntity(int entityId, Click click, Location location, Hand hand) {
        this.entityId = entityId;
        this.click = click;
        this.location = location;
        this.hand = hand;
        log();
    }


    @Override
    public OutPacketBuffer write(ProtocolVersion version) {
        OutPacketBuffer buffer = new OutPacketBuffer(version, version.getPacketCommand(Packets.Serverbound.PLAY_INTERACT_ENTITY));
        switch (version) {
            case VERSION_1_7_10:
                buffer.writeInteger(entityId);
                buffer.writeByte((byte) click.getId());
                break;
            case VERSION_1_8:
                buffer.writeInteger(entityId);
                buffer.writeByte((byte) click.getId());
                if (click == Click.INTERACT_AT) {
                    // position
                    buffer.writeFloat((float) location.getX());
                    buffer.writeFloat((float) location.getY());
                    buffer.writeFloat((float) location.getZ());
                }
                break;
            case VERSION_1_9_4:
                buffer.writeInteger(entityId);
                buffer.writeByte((byte) click.getId());
                if (click == Click.INTERACT_AT) {
                    // position
                    buffer.writeFloat((float) location.getX());
                    buffer.writeFloat((float) location.getY());
                    buffer.writeFloat((float) location.getZ());
                    buffer.writeVarInt(hand.getId());
                }
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Interacting with entity (entityId=%d, click=%s)", entityId, click.name()));
    }

    public enum Click {
        RIGHT(0),
        LEFT(1),
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
