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

package de.bixilon.minosoft.game.datatypes;

import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public enum StatisticCategories {
    MINED(new Identifier("minecraft.mined"), 0),
    CRAFTED(new Identifier("minecraft.crafted"), 1),
    USED(new Identifier("minecraft.used"), 2),
    BROKEN(new Identifier("minecraft.broken"), 3),
    PICKED_UP(new Identifier("minecraft.picked_up"), 4),
    DROPPED(new Identifier("minecraft.dropped"), 5),
    KILLED(new Identifier("minecraft.killed"), 6),
    KILLED_BY(new Identifier("minecraft.killed_by"), 7),
    CUSTOM(new Identifier("minecraft.custom"), 8);


    final Identifier identifier;
    final int id;

    StatisticCategories(Identifier identifier, int id) {
        this.identifier = identifier;
        this.id = id;
    }

    public static StatisticCategories byName(String name, ProtocolVersion version) {
        for (StatisticCategories category : values()) {
            if (category.getIdentifier().isValidName(name, version)) {
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

    public Identifier getIdentifier() {
        return identifier;
    }

    public int getId() {
        return id;
    }
}
