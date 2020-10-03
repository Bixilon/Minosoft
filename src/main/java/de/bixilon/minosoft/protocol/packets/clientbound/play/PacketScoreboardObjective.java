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
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketScoreboardObjective implements ClientboundPacket {
    String name;
    TextComponent value;
    ScoreboardObjectiveActions action;
    ScoreboardObjectiveTypes type;

    @Override
    public boolean read(InByteBuffer buffer) {
        name = buffer.readString();
        if (buffer.getProtocolId() < 7) { // ToDo
            value = buffer.readTextComponent();
        }
        action = ScoreboardObjectiveActions.byId(buffer.readByte());
        if (action == ScoreboardObjectiveActions.CREATE || action == ScoreboardObjectiveActions.UPDATE) {
            if (buffer.getProtocolId() >= 7) { // ToDo
                value = buffer.readTextComponent();
            }
            if (buffer.getProtocolId() >= 12) {
                if (buffer.getProtocolId() >= 346 && buffer.getProtocolId() < 349) {
                    // got removed in these 3 versions
                    return true;
                }
                if (buffer.getProtocolId() < 349) {
                    type = ScoreboardObjectiveTypes.byName(buffer.readString());
                } else {
                    type = ScoreboardObjectiveTypes.byId(buffer.readVarInt());
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
        if (action == ScoreboardObjectiveActions.CREATE || action == ScoreboardObjectiveActions.UPDATE) {
            Log.protocol(String.format("Received scoreboard objective action (action=%s, name=\"%s\", value=\"%s\", type=%s)", action, name, value.getColoredMessage(), type));
        } else {
            Log.protocol(String.format("Received scoreboard objective action (action=%s, name=\"%s\")", action, name));
        }
    }

    public String getName() {
        return name;
    }

    public TextComponent getValue() {
        return value;
    }

    public ScoreboardObjectiveActions getAction() {
        return action;
    }

    public enum ScoreboardObjectiveActions {
        CREATE,
        REMOVE,
        UPDATE;

        public static ScoreboardObjectiveActions byId(int id) {
            return values()[id];
        }

        public int getId() {
            return ordinal();
        }
    }

    public enum ScoreboardObjectiveTypes {
        INTEGER(0, "integer"),
        HEARTS(1, "hearts");

        final int id;
        final String name;

        ScoreboardObjectiveTypes(int id, String name) {
            this.id = id;
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

        public String getName() {
            return name;
        }

        public static ScoreboardObjectiveTypes byId(int id) {
            for (ScoreboardObjectiveTypes type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
