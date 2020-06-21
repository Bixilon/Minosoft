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

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketScoreboardObjective implements ClientboundPacket {
    String name;
    String value;
    ScoreboardObjectiveAction action;
    ScoreboardObjectiveType type;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                name = buffer.readString();
                value = buffer.readString();
                action = ScoreboardObjectiveAction.byId(buffer.readByte());
                break;
            case VERSION_1_8:
                name = buffer.readString();
                action = ScoreboardObjectiveAction.byId(buffer.readByte());
                if (action == ScoreboardObjectiveAction.CREATE || action == ScoreboardObjectiveAction.UPDATE) {
                    value = buffer.readString();
                    type = ScoreboardObjectiveType.byName(buffer.readString());
                }
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received scoreboard objective action (action=%s, name=\"%s\", value=\"%s\"", action.name(), name, value));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public ScoreboardObjectiveAction getAction() {
        return action;
    }

    public enum ScoreboardObjectiveAction {
        CREATE(0),
        REMOVE(1),
        UPDATE(2);

        final int id;

        ScoreboardObjectiveAction(int id) {
            this.id = id;
        }

        public static ScoreboardObjectiveAction byId(int id) {
            for (ScoreboardObjectiveAction a : values()) {
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

    public enum ScoreboardObjectiveType {
        INTEGER("integer"),
        HEARTS("hearts");

        final String name;

        ScoreboardObjectiveType(String name) {
            this.name = name;
        }

        public static ScoreboardObjectiveType byName(String name) {
            for (ScoreboardObjectiveType a : values()) {
                if (a.getName().equals(name)) {
                    return a;
                }
            }
            return null;
        }

        public String getName() {
            return name;
        }
    }
}
