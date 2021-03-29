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

import de.bixilon.minosoft.data.mappings.LegacyResourceLocation;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.Util;

import java.util.Map;
import java.util.TreeMap;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.LOWEST_VERSION_SUPPORTED;

@Deprecated
public class ChangeableResourceLocation extends VersionValueMap<ResourceLocation> {

    public ChangeableResourceLocation(ResourceLocation legacy, ResourceLocation water) {
        this.values.put(LOWEST_VERSION_SUPPORTED, legacy);
        this.values.put(ProtocolDefinition.FLATTING_VERSION_ID, water);
    }

    public ChangeableResourceLocation(Map<Integer, Object> values) {
        super(convertToResourceLocation(values));
    }


    public ChangeableResourceLocation(String name) {
        super(Map.of(LOWEST_VERSION_SUPPORTED, new ResourceLocation(name)));
    }

    private static Map<Integer, ResourceLocation> convertToResourceLocation(Map<Integer, Object> in) {
        TreeMap<Integer, ResourceLocation> out = new TreeMap<>();
        for (Map.Entry<Integer, Object> entry : in.entrySet()) {
            if (entry.getValue() instanceof ResourceLocation resourceLocation) {
                out.put(entry.getKey(), resourceLocation);
                continue;
            }
            if (entry.getValue() instanceof String string) {
                if (Util.doesStringContainsUppercaseLetters(string)) {
                    // just a string but wrapped into a resourceLocation (like old plugin channels MC|BRAND or ...)
                    out.put(entry.getKey(), new LegacyResourceLocation(string));
                    continue;
                }
                out.put(entry.getKey(), new ResourceLocation(string));
                continue;
            }
            throw new IllegalArgumentException(String.format("Type %s is not a string or resource location!", entry.getValue().getClass().getCanonicalName()));
        }
        return out;
    }

    public boolean isValidResourceLocation(ResourceLocation resourceLocation, int versionId) {
        return get(versionId).equals(resourceLocation);
    }
}

