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

import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketCombatEvent implements ClientboundPacket {
    CombatEvent action;

    int duration;
    int playerId;
    int entityId;
    TextComponent message;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                action = CombatEvent.byId(buffer.readVarInt());
                switch (action) {
                    case END_COMBAT:
                        duration = buffer.readVarInt();
                        entityId = buffer.readInt();
                        break;
                    case ENTITY_DEAD:
                        playerId = buffer.readVarInt();
                        entityId = buffer.readInt();
                        message = buffer.readTextComponent();
                        break;
                }
                return true;
        }
        return false;
    }

    @Override
    public void log() {
        switch (action) {
            case ENTER_COMBAT:
                Log.protocol(String.format("Received combat packet (action=%s)", action.name()));
                break;
            case END_COMBAT:
                Log.protocol(String.format("Received combat packet (action=%s, duration=%d, entityId=%d)", action.name(), duration, entityId));
                break;
            case ENTITY_DEAD:
                Log.protocol(String.format("Received combat packet (action=%s, playerId=%d, entityId=%d, message=\"%s\")", action.name(), playerId, entityId, message));
                break;

        }
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }


    public enum CombatEvent {
        ENTER_COMBAT(0),
        END_COMBAT(1),
        ENTITY_DEAD(2);

        final int id;

        CombatEvent(int id) {
            this.id = id;
        }

        public static CombatEvent byId(int id) {
            for (CombatEvent a : values()) {
                if (a.getId() == id) {
                    return a;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
