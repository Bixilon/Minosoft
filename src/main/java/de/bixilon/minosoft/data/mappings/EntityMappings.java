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

package de.bixilon.minosoft.data.mappings;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.entities.EntityInformation;
import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.entities.Entity;

import java.util.HashMap;

public class EntityMappings {
    private final HashBiMap<Class<? extends Entity>, EntityInformation> entityInformationMap;
    private final HashMap<EntityMetaDataFields, Integer> indexMapping;
    private final HashBiMap<Integer, Class<? extends Entity>> entityIdMapping;

    public EntityMappings() {
        this.entityInformationMap = HashBiMap.create();
        this.indexMapping = new HashMap<>();
        this.entityIdMapping = HashBiMap.create();
    }

    public EntityMappings(HashBiMap<Class<? extends Entity>, EntityInformation> entityInformationMap, HashMap<EntityMetaDataFields, Integer> indexMapping, HashBiMap<Integer, Class<? extends Entity>> entityIdMapping) {
        this.entityInformationMap = entityInformationMap;
        this.indexMapping = indexMapping;
        this.entityIdMapping = entityIdMapping;
    }

    public EntityInformation getEntityInformation(Class<? extends Entity> clazz) {
        return entityInformationMap.get(clazz);
    }

    public int getEntityMetaDatIndex(EntityMetaDataFields field) {
        return indexMapping.get(field);
    }

    public Class<? extends Entity> getEntityClassById(int id) {
        return entityIdMapping.get(id);
    }
}
