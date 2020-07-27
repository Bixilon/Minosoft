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

package de.bixilon.minosoft.game.datatypes.objectLoader.effects;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class MobEffects {


    static HashMap<ProtocolVersion, HashBiMap<Integer, MobEffect>> statusEffectMap = new java.util.HashMap<>();

    public static MobEffect byId(int protocolId, ProtocolVersion version) {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            version = ProtocolVersion.VERSION_1_12_2;
        }
        return statusEffectMap.get(version).get(protocolId);
    }

    public static void load(String mod, JsonObject json, ProtocolVersion version) {
        HashBiMap<Integer, MobEffect> versionMapping = HashBiMap.create();
        for (String identifierName : json.keySet()) {
            MobEffect mobEffect = new MobEffect(mod, identifierName);
            versionMapping.put(json.getAsJsonObject(identifierName).get("id").getAsInt(), mobEffect);
        }
        statusEffectMap.put(version, versionMapping);
    }
}