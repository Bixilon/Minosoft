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

package de.bixilon.minosoft.game.datatypes.scoreboard;

import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketScoreboardTeams;

import java.util.Arrays;
import java.util.List;

public class ScoreboardTeam {
    final String name;
    String displayName;
    String prefix;
    String suffix;
    PacketScoreboardTeams.ScoreboardFriendlyFire friendlyFire;
    List<String> players;

    public ScoreboardTeam(String name, String displayName, String prefix, String suffix, PacketScoreboardTeams.ScoreboardFriendlyFire friendlyFire, String[] players) {
        this.name = name;
        this.displayName = displayName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.friendlyFire = friendlyFire;
        this.players = Arrays.asList(players);
    }

    public void updateInformation(String displayName, String prefix, String suffix, PacketScoreboardTeams.ScoreboardFriendlyFire friendlyFire) {
        this.displayName = displayName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.friendlyFire = friendlyFire;
    }

    public String getName() {
        return name;
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

    public PacketScoreboardTeams.ScoreboardFriendlyFire getFriendlyFire() {
        return friendlyFire;
    }

    public void addPlayers(List<String> list) {
        players.addAll(list);
    }

    public void addPlayer(String name) {
        players.add(name);
    }

    public void removePlayers(List<String> list) {
        players.removeAll(list);
    }

    public void removePlayer(String name) {
        players.remove(name);
    }

    public List<String> getPlayers() {
        return players;
    }
}
