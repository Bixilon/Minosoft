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

import de.bixilon.minosoft.game.datatypes.ChatColors;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketScoreboardDisplayScoreboard implements ClientboundPacket {
    ScoreboardAnimations action;
    String scoreName;

    @Override
    public boolean read(InByteBuffer buffer) {
        action = ScoreboardAnimations.byId(buffer.readByte());
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

    public enum ScoreboardAnimations {
        LIST(0),
        SIDEBAR(1),
        BELOW_NAME(2),
        TEAM_BLACK(ChatColors.BLACK.getColor() + 3),
        TEAM_DARK_BLUE(ChatColors.DARK_BLUE.getColor() + 3),
        TEAM_DARK_GREEN(ChatColors.DARK_GREEN.getColor() + 3),
        TEAM_DARK_AQUA(ChatColors.DARK_AQUA.getColor() + 3),
        TEAM_DARK_RED(ChatColors.DARK_RED.getColor() + 3),
        TEAM_DARK_PURPLE(ChatColors.DARK_PURPLE.getColor() + 3),
        TEAM_GOLD(ChatColors.GOLD.getColor() + 3),
        TEAM_GRAY(ChatColors.GRAY.getColor() + 3),
        TEAM_DARK_GRAY(ChatColors.DARK_GRAY.getColor() + 3),
        TEAM_BLUE(ChatColors.BLUE.getColor() + 3),
        TEAM_GREEN(ChatColors.GREEN.getColor() + 3),
        TEAM_AQUA(ChatColors.AQUA.getColor() + 3),
        TEAM_RED(ChatColors.RED.getColor() + 3),
        TEAM_PURPLE(ChatColors.PURPLE.getColor() + 3),
        TEAM_YELLOW(ChatColors.YELLOW.getColor() + 3),
        TEAM_WHITE(ChatColors.WHITE.getColor() + 3);

        final int id;

        ScoreboardAnimations(int id) {
            this.id = id;
        }

        public static ScoreboardAnimations byId(int id) {
            for (ScoreboardAnimations animation : values()) {
                if (animation.getId() == id) {
                    return animation;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
