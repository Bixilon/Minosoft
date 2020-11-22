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

import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.data.text.ChatFormattingCode;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.util.BitByte;

public class PacketTeams implements ClientboundPacket {
    String name;
    TeamActions action;
    ChatComponent displayName;
    ChatComponent prefix;
    ChatComponent suffix;
    boolean friendlyFire;
    boolean seeFriendlyInvisibles;
    TeamCollisionRules collisionRule = TeamCollisionRules.NEVER;
    TeamNameTagVisibilities nameTagVisibility = TeamNameTagVisibilities.ALWAYS;
    ChatFormattingCode formattingCode;
    String[] playerNames;

    @Override
    public boolean read(InByteBuffer buffer) {
        name = buffer.readString();
        action = TeamActions.byId(buffer.readByte());
        if (action == TeamActions.CREATE || action == TeamActions.INFORMATION_UPDATE) {
            displayName = buffer.readTextComponent();
            if (buffer.getVersionId() < 352) {
                prefix = buffer.readTextComponent();
                suffix = buffer.readTextComponent();
            }
            if (buffer.getVersionId() < 100) { //ToDo
                setFriendlyFireByLegacy(buffer.readByte());
            } else {
                byte friendlyFireRaw = buffer.readByte();
                friendlyFire = BitByte.isBitMask(friendlyFireRaw, 0x01);
                seeFriendlyInvisibles = BitByte.isBitMask(friendlyFireRaw, 0x02);
            }
            if (buffer.getVersionId() >= 11) {
                nameTagVisibility = TeamNameTagVisibilities.byName(buffer.readString());
                if (buffer.getVersionId() >= 100) { //ToDo
                    collisionRule = TeamCollisionRules.byName(buffer.readString());
                }
                if (buffer.getVersionId() < 352) {
                    formattingCode = ChatColors.getFormattingById(buffer.readByte());
                } else {
                    formattingCode = ChatColors.getFormattingById(buffer.readVarInt());
                }
            }
            if (buffer.getVersionId() >= 375) {
                prefix = buffer.readTextComponent();
                suffix = buffer.readTextComponent();
            }
        }
        if (action == TeamActions.CREATE || action == TeamActions.PLAYER_ADD || action == TeamActions.PLAYER_REMOVE) {
            int playerCount;
            if (buffer.getVersionId() < 7) {
                playerCount = buffer.readShort();
            } else {
                playerCount = buffer.readVarInt();
            }
            playerNames = new String[playerCount];
            for (int i = 0; i < playerCount; i++) {
                playerNames[i] = buffer.readString();
            }
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    private void setFriendlyFireByLegacy(byte raw) {
        switch (raw) {
            case 0 -> friendlyFire = false;
            case 1 -> friendlyFire = true;
            case 2 -> {
                friendlyFire = false;
                seeFriendlyInvisibles = true;
            }
        }
        // ToDo: seeFriendlyInvisibles for case 0 and 1
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received scoreboard Team update (name=\"%s\", action=%s, displayName=\"%s\", prefix=\"%s\", suffix=\"%s\", friendlyFire=%s, seeFriendlyInvisibiles=%s, playerCount=%s)", name, action, displayName, prefix, suffix, friendlyFire, seeFriendlyInvisibles, ((playerNames == null) ? null : playerNames.length)));
    }

    public String getName() {
        return name;
    }

    public TeamActions getAction() {
        return action;
    }

    public ChatComponent getDisplayName() {
        return displayName;
    }

    public ChatComponent getPrefix() {
        return prefix;
    }

    public ChatComponent getSuffix() {
        return suffix;
    }

    public boolean isFriendlyFireEnabled() {
        return friendlyFire;
    }

    public boolean isSeeingFriendlyInvisibles() {
        return seeFriendlyInvisibles;
    }

    public ChatFormattingCode getFormattingCode() {
        return formattingCode;
    }

    public TeamCollisionRules getCollisionRule() {
        return collisionRule;
    }

    public TeamNameTagVisibilities getNameTagVisibility() {
        return nameTagVisibility;
    }

    public String[] getPlayerNames() {
        return playerNames;
    }

    public enum TeamActions {
        CREATE,
        REMOVE,
        INFORMATION_UPDATE,
        PLAYER_ADD,
        PLAYER_REMOVE;

        public static TeamActions byId(int id) {
            return values()[id];
        }
    }

    public enum TeamNameTagVisibilities {
        ALWAYS("always"),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam"),
        NEVER("never");

        final String name;

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
            return name;
        }
    }

    public enum TeamCollisionRules {
        ALWAYS("always"),
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        PUSH_OWN_TEAM("pushOwnOwnTeam"),
        NEVER("never");

        final String name;

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
            return name;
        }
    }
}
