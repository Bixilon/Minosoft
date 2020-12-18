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

import de.bixilon.minosoft.data.mappings.ModIdentifier;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.Map;
import java.util.TreeMap;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.LOWEST_VERSION_SUPPORTED;

public class ChangeableIdentifier extends VersionValueMap<ModIdentifier> {

    public ChangeableIdentifier(ModIdentifier legacy, ModIdentifier water) {
        this.values.put(LOWEST_VERSION_SUPPORTED, legacy);
        this.values.put(ProtocolDefinition.FLATTING_VERSION_ID, water);
    }

    public ChangeableIdentifier(Map<Integer, String> values) {
        super(convertToIdentifier(values));
    }


    public ChangeableIdentifier(String name) {
        super(Map.of(LOWEST_VERSION_SUPPORTED, new ModIdentifier(name)));
    }

    private static Map<Integer, ModIdentifier> convertToIdentifier(Map<Integer, String> in) {
        TreeMap<Integer, ModIdentifier> out = new TreeMap<>();
        for (Map.Entry<Integer, String> entry : in.entrySet()) {
            out.put(entry.getKey(), new ModIdentifier(entry.getValue()));
        }
        return out;
    }

    public boolean isValidIdentifier(ModIdentifier identifier, int versionId) {
        return get(versionId).equals(identifier);
    }
}

