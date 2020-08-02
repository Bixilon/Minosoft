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

import java.util.HashMap;

public class ScoreboardManager {
    final HashMap<String, Team> teams;
    final HashMap<String, ScoreboardObjective> objectives;

    public ScoreboardManager() {
        teams = new HashMap<>();
        objectives = new HashMap<>();
    }

    public void addTeam(Team team) {
        teams.put(team.getName(), team);
    }

    public Team getTeam(String name) {
        return teams.get(name);
    }

    public void removeTeam(String name) {
        teams.remove(name);
    }

    public void addObjective(ScoreboardObjective objective) {
        objectives.put(objective.getObjectiveName(), objective);
    }

    public void removeObjective(String name) {
        objectives.remove(name);
    }

    public ScoreboardObjective getObjective(String objective) {
        return objectives.get(objective);
    }
}
