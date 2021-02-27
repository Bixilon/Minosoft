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

import de.bixilon.minosoft.data.ChangeableResourceLocation;
import de.bixilon.minosoft.data.mappings.ResourceLocation;

@Deprecated
public enum StatisticCategories {
    MINED(new ChangeableResourceLocation("minecraft.mined"), 0),
    CRAFTED(new ChangeableResourceLocation("minecraft.crafted"), 1),
    USED(new ChangeableResourceLocation("minecraft.used"), 2),
    BROKEN(new ChangeableResourceLocation("minecraft.broken"), 3),
    PICKED_UP(new ChangeableResourceLocation("minecraft.picked_up"), 4),
    DROPPED(new ChangeableResourceLocation("minecraft.dropped"), 5),
    KILLED(new ChangeableResourceLocation("minecraft.killed"), 6),
    KILLED_BY(new ChangeableResourceLocation("minecraft.killed_by"), 7),
    CUSTOM(new ChangeableResourceLocation("minecraft.custom"), 8);


    private final ChangeableResourceLocation changeableResourceLocation;
    private final int id;

    StatisticCategories(ChangeableResourceLocation changeableResourceLocation, int id) {
        this.changeableResourceLocation = changeableResourceLocation;
        this.id = id;
    }

    public static StatisticCategories byName(ResourceLocation resourceLocation, int versionId) {
        for (StatisticCategories category : values()) {
            if (category.getChangeableResourceLocation().isValidResourceLocation(resourceLocation, versionId)) {
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

    public ChangeableResourceLocation getChangeableResourceLocation() {
        return this.changeableResourceLocation;
    }

    public int getId() {
        return this.id;
    }
}
