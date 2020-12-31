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

package de.bixilon.minosoft.data.scoreboard;

import de.bixilon.minosoft.data.text.ChatComponent;

import java.util.HashMap;

public class ScoreboardObjective {
    private final String objectiveName;
    private final HashMap<String, ScoreboardScore> scores = new HashMap<>();
    ChatComponent objectiveValue;

    public ScoreboardObjective(String objectiveName, ChatComponent objectiveValue) {
        this.objectiveName = objectiveName;
        this.objectiveValue = objectiveValue;
    }

    public String getObjectiveName() {
        return this.objectiveName;
    }

    public ChatComponent getObjectiveValue() {
        return this.objectiveValue;
    }

    public HashMap<String, ScoreboardScore> getScores() {
        return this.scores;
    }

    public void addScore(ScoreboardScore score) {
        if (this.scores.containsKey(score.getItemName())) {
            // update
            this.scores.get(score.getItemName()).setScoreName(score.getScoreName());
            this.scores.get(score.getItemName()).setScore(score.getScore());
            return;
        }
        this.scores.put(score.getItemName(), score);
    }

    public void removeScore(String itemName) {
        this.scores.remove(itemName);
    }

    public ScoreboardScore getScore(String itemName) {
        return this.scores.get(itemName);
    }

    public void setValue(ChatComponent value) {
        this.objectiveValue = value;
    }
}
