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
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketScoreboardDisplayScoreboard implements ClientboundPacket {
    ScoreboardAnimation action;
    String scoreName;

    @Override
    public boolean read(InByteBuffer buffer) {
        action = ScoreboardAnimation.byId(buffer.readByte());
        scoreName = buffer.readString();
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received display scoreboard packet (position=%s, scoreName=\"%s\"", action, scoreName));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public enum ScoreboardAnimation {
        LIST(0),
        SIDEBAR(1),
        BELOW_NAME(2),
        TEAM_BLACK(ChatColor.BLACK.getColor() + 3),
        TEAM_DARK_BLUE(ChatColor.DARK_BLUE.getColor() + 3),
        TEAM_DARK_GREEN(ChatColor.DARK_GREEN.getColor() + 3),
        TEAM_DARK_AQUA(ChatColor.DARK_AQUA.getColor() + 3),
        TEAM_DARK_RED(ChatColor.DARK_RED.getColor() + 3),
        TEAM_DARK_PURPLE(ChatColor.DARK_PURPLE.getColor() + 3),
        TEAM_GOLD(ChatColor.GOLD.getColor() + 3),
        TEAM_GRAY(ChatColor.GRAY.getColor() + 3),
        TEAM_DARK_GRAY(ChatColor.DARK_GRAY.getColor() + 3),
        TEAM_BLUE(ChatColor.BLUE.getColor() + 3),
        TEAM_GREEN(ChatColor.GREEN.getColor() + 3),
        TEAM_AQUA(ChatColor.AQUA.getColor() + 3),
        TEAM_RED(ChatColor.RED.getColor() + 3),
        TEAM_PURPLE(ChatColor.PURPLE.getColor() + 3),
        TEAM_YELLOW(ChatColor.YELLOW.getColor() + 3),
        TEAM_WHITE(ChatColor.WHITE.getColor() + 3);

        final int id;

        ScoreboardAnimation(int id) {
            this.id = id;
        }

        public static ScoreboardAnimation byId(int id) {
            for (ScoreboardAnimation a : values()) {
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
