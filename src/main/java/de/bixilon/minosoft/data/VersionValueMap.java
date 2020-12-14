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

import de.bixilon.minosoft.data.mappings.versions.Versions;

import java.util.Map;
import java.util.TreeMap;

public class VersionValueMap<V> {
    TreeMap<Integer, V> values = new TreeMap<>();

    public VersionValueMap() {
    }

    public VersionValueMap(MapSet<Integer, V>[] sets, boolean unused) {
        for (MapSet<Integer, V> set : sets) {
            this.values.put(set.getKey(), set.getValue());
        }
    }

    public VersionValueMap(V value) {
        this.values.put(Versions.LOWEST_VERSION_SUPPORTED.getVersionId(), value);
    }

    public V get(int versionId) {
        Map.Entry<Integer, V> value = this.values.lowerEntry(versionId);
        if (value == null) {
            return null;
        }
        return value.getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        VersionValueMap<V> their = (VersionValueMap<V>) obj;
        return getAll().equals(their.getAll());
    }

    public TreeMap<Integer, V> getAll() {
        return this.values;
    }
}
