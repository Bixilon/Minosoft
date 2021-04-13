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

package de.bixilon.minosoft.data.scoreboard;

import de.bixilon.minosoft.data.text.ChatComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Team {
    private final String name;
    private final ArrayList<String> players;
    ChatComponent displayName;
    ChatComponent prefix;
    ChatComponent suffix;
    boolean friendlyFire;
    boolean seeFriendlyInvisibles;

    public Team(String name, ChatComponent displayName, ChatComponent prefix, ChatComponent suffix, boolean friendlyFire, boolean seeFriendlyInvisibles, String[] players) {
        this.name = name;
        this.displayName = displayName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.friendlyFire = friendlyFire;
        this.seeFriendlyInvisibles = seeFriendlyInvisibles;
        this.players = new ArrayList<>(Arrays.asList(players));
    }

    public void updateInformation(ChatComponent displayName, ChatComponent prefix, ChatComponent suffix, boolean friendlyFire, boolean seeFriendlyInvisibles) {
        this.displayName = displayName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.friendlyFire = friendlyFire;
        this.seeFriendlyInvisibles = seeFriendlyInvisibles;
    }

    public String getName() {
        return this.name;
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

    public void addPlayers(List<String> list) {
        this.players.addAll(list);
    }

    public void addPlayer(String name) {
        this.players.add(name);
    }

    public void removePlayers(List<String> list) {
        this.players.removeAll(list);
    }

    public void removePlayer(String name) {
        this.players.remove(name);
    }

    public ArrayList<String> getPlayers() {
        return this.players;
    }
}
