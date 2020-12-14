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
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketScoreboardObjective implements ClientboundPacket {
    String name;
    ChatComponent value;
    ScoreboardObjectiveActions action;
    ScoreboardObjectiveTypes type;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.name = buffer.readString();
        if (buffer.getVersionId() < 7) { // ToDo
            this.value = buffer.readChatComponent();
        }
        this.action = ScoreboardObjectiveActions.byId(buffer.readUnsignedByte());
        if (this.action == ScoreboardObjectiveActions.CREATE || this.action == ScoreboardObjectiveActions.UPDATE) {
            if (buffer.getVersionId() >= 7) { // ToDo
                this.value = buffer.readChatComponent();
            }
            if (buffer.getVersionId() >= 12) {
                if (buffer.getVersionId() >= 346 && buffer.getVersionId() < 349) {
                    // got removed in these 3 versions
                    return true;
                }
                if (buffer.getVersionId() < 349) {
                    this.type = ScoreboardObjectiveTypes.byName(buffer.readString());
                } else {
                    this.type = ScoreboardObjectiveTypes.byId(buffer.readVarInt());
                }
            }
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        if (this.action == ScoreboardObjectiveActions.CREATE || this.action == ScoreboardObjectiveActions.UPDATE) {
            Log.protocol(String.format("[IN] Received scoreboard objective action (action=%s, name=\"%s\", value=\"%s\", type=%s)", this.action, this.name, this.value.getANSIColoredMessage(), this.type));
        } else {
            Log.protocol(String.format("[IN] Received scoreboard objective action (action=%s, name=\"%s\")", this.action, this.name));
        }
    }

    public String getName() {
        return this.name;
    }

    public ChatComponent getValue() {
        return this.value;
    }

    public ScoreboardObjectiveActions getAction() {
        return this.action;
    }

    public enum ScoreboardObjectiveActions {
        CREATE,
        REMOVE,
        UPDATE;

        private static final ScoreboardObjectiveActions[] SCOREBOARD_OBJECTIVE_ACTIONS = values();

        public static ScoreboardObjectiveActions byId(int id) {
            return SCOREBOARD_OBJECTIVE_ACTIONS[id];
        }
    }

    public enum ScoreboardObjectiveTypes {
        INTEGER("integer"),
        HEARTS("hearts");

        private static final ScoreboardObjectiveTypes[] SCOREBOARD_OBJECTIVE_TYPES = values();
        final String name;

        ScoreboardObjectiveTypes(String name) {
            this.name = name;
        }

        public static ScoreboardObjectiveTypes byName(String name) {
            for (ScoreboardObjectiveTypes type : values()) {
                if (type.getName().equals(name)) {
                    return type;
                }
            }
            return null;
        }

        public static ScoreboardObjectiveTypes byId(int id) {
            return SCOREBOARD_OBJECTIVE_TYPES[id];
        }

        public String getName() {
            return this.name;
        }
    }
}
