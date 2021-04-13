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

import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketScoreboardDisplayScoreboard extends PlayClientboundPacket {
    private final ScoreboardAnimations action;
    private final String scoreName;

    public PacketScoreboardDisplayScoreboard(PlayInByteBuffer buffer) {
        this.action = ScoreboardAnimations.byId(buffer.readUnsignedByte());
        this.scoreName = buffer.readString();
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received display scoreboard packet (position=%s, scoreName=\"%s\"", this.action, this.scoreName));
    }

    public enum ScoreboardAnimations {
        LIST,
        SIDEBAR,
        BELOW_NAME,
        TEAM_BLACK,
        TEAM_DARK_BLUE,
        TEAM_DARK_GREEN,
        TEAM_DARK_AQUA,
        TEAM_DARK_RED,
        TEAM_DARK_PURPLE,
        TEAM_GOLD,
        TEAM_GRAY,
        TEAM_DARK_GRAY,
        TEAM_BLUE,
        TEAM_GREEN,
        TEAM_AQUA,
        TEAM_RED,
        TEAM_PURPLE,
        TEAM_YELLOW,
        TEAM_WHITE;

        private static final ScoreboardAnimations[] SCOREBOARD_ANIMATIONS = values();

        public static ScoreboardAnimations byId(int id) {
            return SCOREBOARD_ANIMATIONS[id];
        }
    }
}
