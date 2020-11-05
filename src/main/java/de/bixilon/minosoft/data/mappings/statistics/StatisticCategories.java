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

package de.bixilon.minosoft.data.mappings.statistics;

import de.bixilon.minosoft.data.ChangeableIdentifier;

public enum StatisticCategories {
    MINED(new ChangeableIdentifier("minecraft.mined"), 0),
    CRAFTED(new ChangeableIdentifier("minecraft.crafted"), 1),
    USED(new ChangeableIdentifier("minecraft.used"), 2),
    BROKEN(new ChangeableIdentifier("minecraft.broken"), 3),
    PICKED_UP(new ChangeableIdentifier("minecraft.picked_up"), 4),
    DROPPED(new ChangeableIdentifier("minecraft.dropped"), 5),
    KILLED(new ChangeableIdentifier("minecraft.killed"), 6),
    KILLED_BY(new ChangeableIdentifier("minecraft.killed_by"), 7),
    CUSTOM(new ChangeableIdentifier("minecraft.custom"), 8);

    final ChangeableIdentifier changeableIdentifier;
    final int id;

    StatisticCategories(ChangeableIdentifier changeableIdentifier, int id) {
        this.changeableIdentifier = changeableIdentifier;
        this.id = id;
    }

    public static StatisticCategories byName(String name, int versionId) {
        for (StatisticCategories category : values()) {
            if (category.getChangeableIdentifier().isValidName(name, versionId)) {
                return category;
            }
        }
        return null;
    }

    public static StatisticCategories byId(int id) {
        for (StatisticCategories category : values()) {
            if (category.getId() == id) {
                return category;
            }
        }
        return null;
    }

    public ChangeableIdentifier getChangeableIdentifier() {
        return changeableIdentifier;
    }

    public int getId() {
        return id;
    }
}
