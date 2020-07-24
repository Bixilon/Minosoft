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

package de.bixilon.minosoft.game.datatypes.enchantments;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Enchantments {

    static ArrayList<Enchantment> enchantmentList = new ArrayList<>();
    static HashMap<ProtocolVersion, HashBiMap<Integer, Enchantment>> enchantmentMap = new HashMap<>();

    public static Enchantment getEnchantmentBdyI(int protocolId, ProtocolVersion version) {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            version = ProtocolVersion.VERSION_1_12_2;
        }
        return enchantmentMap.get(version).get(protocolId);
    }

    public static void load(String mod, JSONObject json, ProtocolVersion version) {
        HashBiMap<Integer, Enchantment> versionMapping = HashBiMap.create();
        for (Iterator<String> identifiers = json.keys(); identifiers.hasNext(); ) {
            String identifierName = identifiers.next();
            Enchantment enchantment = new Enchantment(mod, identifierName);
            if (enchantmentList.contains(enchantment)) {
                enchantment = enchantmentList.get(enchantmentList.indexOf(enchantment));
            }
            versionMapping.put(json.getJSONObject(identifierName).getInt("id"), enchantment);
        }
        enchantmentMap.put(version, versionMapping);
    }

}
