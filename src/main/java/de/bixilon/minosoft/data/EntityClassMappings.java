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

package de.bixilon.minosoft.data;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.entities.entities.Zombie;
import javafx.util.Pair;

public final class EntityClassMappings {
    public static final HashBiMap<Class<? extends Entity>, Pair<String, String>> ENTITY_CLASS_MAPPINGS = HashBiMap.create();

    static {
        ENTITY_CLASS_MAPPINGS.put(Zombie.class, new Pair<>("minecraft", "zombie"));
    }

    public static Class<? extends Entity> getByIdentifier(String mod, String identifier) {
        return ENTITY_CLASS_MAPPINGS.inverse().get(new Pair<>(mod, identifier));
    }

}
