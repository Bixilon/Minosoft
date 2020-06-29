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

import de.bixilon.minosoft.game.datatypes.ChatColor;
import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketScoreboardTeams implements ClientboundPacket {
    String name;
    ScoreboardTeamAction action;
    String displayName;
    String prefix;
    String suffix;
    ScoreboardFriendlyFire friendlyFire;
    ScoreboardNameTagVisibility nameTagVisibility;
    TextComponent.ChatAttributes color;
    String[] playerNames;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                name = buffer.readString();
                action = ScoreboardTeamAction.byId(buffer.readByte());
                if (action == ScoreboardTeamAction.CREATE || action == ScoreboardTeamAction.INFORMATION_UPDATE) {
                    displayName = buffer.readString();
                    prefix = buffer.readString();
                    suffix = buffer.readString();
                    friendlyFire = ScoreboardFriendlyFire.byId(buffer.readByte());
                    // default values
                    nameTagVisibility = ScoreboardNameTagVisibility.ALWAYS;
                    color = TextComponent.ChatAttributes.WHITE;
                }
                if (action == ScoreboardTeamAction.CREATE || action == ScoreboardTeamAction.PLAYER_ADD || action == ScoreboardTeamAction.PLAYER_REMOVE) {
                    short playerCount = buffer.readShort();
                    playerNames = new String[playerCount];
                    for (int i = 0; i < playerCount; i++) {
                        playerNames[i] = buffer.readString();
                    }
                }
                return true;
            case VERSION_1_8:
                name = buffer.readString();
                action = ScoreboardTeamAction.byId(buffer.readByte());
                if (action == ScoreboardTeamAction.CREATE || action == ScoreboardTeamAction.INFORMATION_UPDATE) {
                    displayName = buffer.readString();
                    prefix = buffer.readString();
                    suffix = buffer.readString();
                    friendlyFire = ScoreboardFriendlyFire.byId(buffer.readByte());
                    nameTagVisibility = ScoreboardNameTagVisibility.byName(buffer.readString());
                    color = TextComponent.ChatAttributes.byColor(ChatColor.byId(buffer.readByte()));
                }
                if (action == ScoreboardTeamAction.CREATE || action == ScoreboardTeamAction.PLAYER_ADD || action == ScoreboardTeamAction.PLAYER_REMOVE) {
                    int playerCount = buffer.readVarInt();
                    playerNames = new String[playerCount];
                    for (int i = 0; i < playerCount; i++) {
                        playerNames[i] = buffer.readString();
                    }
                }
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received scoreboard Team update (name=\"%s\", action=%s, displayName=\"%s\", prefix=\"%s\", suffix=\"%s\", friendlyFire=%s, playerCount=%s)", name, action.name(), displayName, prefix, suffix, (friendlyFire == null ? "null" : friendlyFire.name()), ((playerNames == null) ? "null" : playerNames.length)));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public String getName() {
        return name;
    }

    public ScoreboardTeamAction getAction() {
        return action;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public ScoreboardFriendlyFire getFriendlyFire() {
        return friendlyFire;
    }

    public String[] getPlayerNames() {
        return playerNames;
    }

    public enum ScoreboardTeamAction {
        CREATE(0),
        REMOVE(1),
        INFORMATION_UPDATE(2),
        PLAYER_ADD(3),
        PLAYER_REMOVE(4);

        final int id;

        ScoreboardTeamAction(int id) {
            this.id = id;
        }

        public static ScoreboardTeamAction byId(int id) {
            for (ScoreboardTeamAction a : values()) {
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

    public enum ScoreboardFriendlyFire {
        OFF(0),
        ON(1),
        SEE_FRIENDLY_INVISIBLES(3);

        final int id;

        ScoreboardFriendlyFire(int id) {
            this.id = id;
        }

        public static ScoreboardFriendlyFire byId(int id) {
            for (ScoreboardFriendlyFire f : values()) {
                if (f.getId() == id) {
                    return f;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    public enum ScoreboardNameTagVisibility {
        ALWAYS("always"),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam"),
        NEVER("never");

        final String name;

        ScoreboardNameTagVisibility(String name) {
            this.name = name;
        }

        public static ScoreboardNameTagVisibility byName(String name) {
            for (ScoreboardNameTagVisibility v : values()) {
                if (v.getName().equals(name)) {
                    return v;
                }
            }
            return null;
        }

        public String getName() {
            return name;
        }
    }
}
