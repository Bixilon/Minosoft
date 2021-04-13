/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.scoreboard.Team;
import de.bixilon.minosoft.data.text.ChatCode;
import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.logging.Log;

import java.util.Arrays;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketTeams extends PlayClientboundPacket {
    String name;
    TeamActions action;
    ChatComponent displayName;
    ChatComponent prefix;
    ChatComponent suffix;
    boolean friendlyFire;
    boolean seeFriendlyInvisibles;
    TeamCollisionRules collisionRule = TeamCollisionRules.NEVER;
    TeamNameTagVisibilities nameTagVisibility = TeamNameTagVisibilities.ALWAYS;
    ChatCode formattingCode;
    String[] playerNames;

    public PacketTeams(PlayInByteBuffer buffer) {
        this.name = buffer.readString();
        this.action = TeamActions.byId(buffer.readUnsignedByte());
        if (this.action == TeamActions.CREATE || this.action == TeamActions.INFORMATION_UPDATE) {
            this.displayName = buffer.readChatComponent();
            if (buffer.getVersionId() < V_18W01A) {
                this.prefix = buffer.readChatComponent();
                this.suffix = buffer.readChatComponent();
            }
            if (buffer.getVersionId() < V_16W06A) { // ToDo
                setFriendlyFireByLegacy(buffer.readByte());
            } else {
                byte friendlyFireRaw = buffer.readByte();
                this.friendlyFire = BitByte.isBitMask(friendlyFireRaw, 0x01);
                this.seeFriendlyInvisibles = BitByte.isBitMask(friendlyFireRaw, 0x02);
            }
            if (buffer.getVersionId() >= V_14W07A) {
                this.nameTagVisibility = TeamNameTagVisibilities.byName(buffer.readString());
                if (buffer.getVersionId() >= V_16W06A) { // ToDo
                    this.collisionRule = TeamCollisionRules.byName(buffer.readString());
                }
                if (buffer.getVersionId() < V_18W01A) {
                    this.formattingCode = ChatColors.getFormattingById(buffer.readByte());
                } else {
                    this.formattingCode = ChatColors.getFormattingById(buffer.readVarInt());
                }
            }
            if (buffer.getVersionId() >= V_18W20A) {
                this.prefix = buffer.readChatComponent();
                this.suffix = buffer.readChatComponent();
            }
        }
        if (this.action == TeamActions.CREATE || this.action == TeamActions.PLAYER_ADD || this.action == TeamActions.PLAYER_REMOVE) {
            int playerCount;
            if (buffer.getVersionId() < V_14W04A) {
                playerCount = buffer.readUnsignedShort();
            } else {
                playerCount = buffer.readVarInt();
            }
            this.playerNames = new String[playerCount];
            for (int i = 0; i < playerCount; i++) {
                this.playerNames[i] = buffer.readString();
            }
        }
    }

    @Override
    public void handle(PlayConnection connection) {
        switch (getAction()) {
            case CREATE -> connection.getScoreboardManager().addTeam(new Team(getName(), getDisplayName(), getPrefix(), getSuffix(), isFriendlyFireEnabled(), isSeeingFriendlyInvisibles(), getPlayerNames()));
            case INFORMATION_UPDATE -> connection.getScoreboardManager().getTeam(getName()).updateInformation(getDisplayName(), getPrefix(), getSuffix(), isFriendlyFireEnabled(), isSeeingFriendlyInvisibles());
            case REMOVE -> connection.getScoreboardManager().removeTeam(getName());
            case PLAYER_ADD -> connection.getScoreboardManager().getTeam(getName()).addPlayers(Arrays.asList(getPlayerNames()));
            case PLAYER_REMOVE -> connection.getScoreboardManager().getTeam(getName()).removePlayers(Arrays.asList(getPlayerNames()));
        }
    }

    private void setFriendlyFireByLegacy(byte raw) {
        switch (raw) {
            case 0 -> this.friendlyFire = false;
            case 1 -> this.friendlyFire = true;
            case 2 -> {
                this.friendlyFire = false;
                this.seeFriendlyInvisibles = true;
            }
        }
        // ToDo: seeFriendlyInvisibles for case 0 and 1
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received scoreboard Team update (name=\"%s\", action=%s, displayName=\"%s\", prefix=\"%s\", suffix=\"%s\", friendlyFire=%s, seeFriendlyInvisibiles=%s, playerCount=%s)", this.name, this.action, this.displayName, this.prefix, this.suffix, this.friendlyFire, this.seeFriendlyInvisibles, ((this.playerNames == null) ? null : this.playerNames.length)));
    }

    public String getName() {
        return this.name;
    }

    public TeamActions getAction() {
        return this.action;
    }

    public ChatComponent getDisplayName() {
        return this.displayName;
    }

    public ChatComponent getPrefix() {
        return this.prefix;
    }

    public ChatComponent getSuffix() {
        return this.suffix;
    }

    public boolean isFriendlyFireEnabled() {
        return this.friendlyFire;
    }

    public boolean isSeeingFriendlyInvisibles() {
        return this.seeFriendlyInvisibles;
    }

    public ChatCode getFormattingCode() {
        return this.formattingCode;
    }

    public TeamCollisionRules getCollisionRule() {
        return this.collisionRule;
    }

    public TeamNameTagVisibilities getNameTagVisibility() {
        return this.nameTagVisibility;
    }

    public String[] getPlayerNames() {
        return this.playerNames;
    }

    public enum TeamActions {
        CREATE,
        REMOVE,
        INFORMATION_UPDATE,
        PLAYER_ADD,
        PLAYER_REMOVE;

        private static final TeamActions[] TEAM_ACTIONS = values();

        public static TeamActions byId(int id) {
            return TEAM_ACTIONS[id];
        }
    }

    public enum TeamNameTagVisibilities {
        ALWAYS("always"),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam"),
        NEVER("never");

   private final String name;

        TeamNameTagVisibilities(String name) {
            this.name = name;
        }

        public static TeamNameTagVisibilities byName(String name) {
            for (TeamNameTagVisibilities visibility : values()) {
                if (visibility.getName().equals(name)) {
                    return visibility;
                }
            }
            return null;
        }

        public String getName() {
            return this.name;
        }
    }

    public enum TeamCollisionRules {
        ALWAYS("always"),
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        PUSH_OWN_TEAM("pushOwnOwnTeam"),
        NEVER("never");

        private final String name;

        TeamCollisionRules(String name) {
            this.name = name;
        }

        public static TeamCollisionRules byName(String name) {
            for (TeamCollisionRules rule : values()) {
                if (rule.getName().equals(name)) {
                    return rule;
                }
            }
            return null;
        }

        public String getName() {
            return this.name;
        }
    }
}
