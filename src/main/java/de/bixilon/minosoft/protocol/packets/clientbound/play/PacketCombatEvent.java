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

import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketCombatEvent extends ClientboundPacket {
    CombatEvents action;

    int duration;
    int playerId;
    int entityId;
    ChatComponent message;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.action = CombatEvents.byId(buffer.readVarInt());
        switch (this.action) {
            case END_COMBAT -> {
                this.duration = buffer.readVarInt();
                this.entityId = buffer.readInt();
            }
            case ENTITY_DEAD -> {
                this.playerId = buffer.readVarInt();
                this.entityId = buffer.readInt();
                this.message = buffer.readChatComponent();
            }
        }
        return true;
    }

    @Override
    public void log() {
        switch (this.action) {
            case ENTER_COMBAT -> Log.protocol(String.format("[IN] Received combat packet (action=%s)", this.action));
            case END_COMBAT -> Log.protocol(String.format("[IN] Received combat packet (action=%s, duration=%d, entityId=%d)", this.action, this.duration, this.entityId));
            case ENTITY_DEAD -> Log.protocol(String.format("[IN] Received combat packet (action=%s, playerId=%d, entityId=%d, message=\"%s\")", this.action, this.playerId, this.entityId, this.message));
        }
    }

    public enum CombatEvents {
        ENTER_COMBAT,
        END_COMBAT,
        ENTITY_DEAD;

        private static final CombatEvents[] COMBAT_EVENTS = values();

        public static CombatEvents byId(int id) {
            return COMBAT_EVENTS[id];
        }
    }
}
