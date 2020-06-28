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

import de.bixilon.minosoft.protocol.protocol.Protocol;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class VersionValueMap<V> {
    HashMap<ProtocolVersion, V> values = new HashMap<>();

    public VersionValueMap() {
        
    }


    public VersionValueMap(MapSet<ProtocolVersion, V>[] sets, boolean unused) {
        for (MapSet<ProtocolVersion, V> set : sets) {
            values.put(set.getKey(), set.getValue());
        }
    }

    public VersionValueMap(V value) {
        values.put(Protocol.getLowestVersionSupported(), value);
    }

    public ProtocolVersion getSuitableProtocolVersion(ProtocolVersion version) {
        for (int i = Arrays.binarySearch(ProtocolVersion.versionMappingArray, version); i >= 0; i--) {
            // count backwards to find best version
            if (values.containsKey(ProtocolVersion.versionMappingArray[i])) {
                return ProtocolVersion.versionMappingArray[i];
            }
        }
        return Protocol.getLowestVersionSupported();
    }

    public V get(ProtocolVersion version) {
        return values.get(getSuitableProtocolVersion(version));
    }

    public HashMap<ProtocolVersion, V> getAll() {
        return values;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        VersionValueMap<V> that = (VersionValueMap<V>) obj;
        for (Map.Entry<ProtocolVersion, V> set : values.entrySet()) {
            V theirValue = that.get(set.getKey());
            if (theirValue == null) {
                continue;
            }
            if (set.getValue().equals(theirValue)) {
                return true;
            }
        }
        return false;
    }
}
