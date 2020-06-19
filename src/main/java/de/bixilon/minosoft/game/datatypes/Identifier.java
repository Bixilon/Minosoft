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

import java.util.*;

public class Identifier {
    HashMap<ProtocolVersion, List<String>> names = new HashMap<>();
    String mod = "minecraft"; // by default minecraft

    public Identifier(String mod, String legacy, String water) { // water for water update name (post 1.13.x)
        this.mod = mod;
        names.put(Protocol.getLowestVersionSupported(), Collections.singletonList(legacy)); // lowest version - water update (1.7.10 - 1.12.2)
        names.put(ProtocolVersion.VERSION_1_13_2, Collections.singletonList(water)); // 1.13 - newest version
    }

    public Identifier(String legacy, String water) {
        names.put(Protocol.getLowestVersionSupported(), Collections.singletonList(legacy));
        names.put(ProtocolVersion.VERSION_1_13_2, Collections.singletonList(water));
    }

    public Identifier(String name) {
        names.put(Protocol.getLowestVersionSupported(), Collections.singletonList(name));
    }

    public String getMod() {
        return mod;
    }

    public ProtocolVersion getSuitableProtocolVersion(ProtocolVersion v) {
        for (int i = Arrays.binarySearch(ProtocolVersion.versionMappingArray, v); i >= 0; i--) {
            // count backwards to find best version
            if (names.containsKey(ProtocolVersion.versionMappingArray[i])) {
                return ProtocolVersion.versionMappingArray[i];
            }
        }
        return Protocol.getLowestVersionSupported();
    }

    public List<String> getAll(ProtocolVersion v) {
        return names.get(getSuitableProtocolVersion(v));
    }

    public String get(ProtocolVersion v) {
        return getAll(v).get(0);
    }


    public HashMap<ProtocolVersion, List<String>> getAll() {
        return names;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        Identifier that = (Identifier) obj;
        for (Map.Entry<ProtocolVersion, List<String>> set : names.entrySet()) {
            List<String> theirList = that.getAll().get(set.getKey());
            if (theirList == null) {
                continue;
            }
            for (String name : set.getValue()) {
                if (theirList.contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
