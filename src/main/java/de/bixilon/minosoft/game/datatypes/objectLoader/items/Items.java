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

package de.bixilon.minosoft.game.datatypes.objectLoader.items;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;
import java.util.HashSet;

public class Items {

    static HashSet<Item> itemList = new HashSet<>();
    static HashMap<ProtocolVersion, HashBiMap<Integer, Item>> itemMap = new HashMap<>(); // version -> (protocolId > Item)

    public static Item getItemByLegacy(int protocolId, int protocolMetaData) {
        int itemId = protocolId << 4;
        if (protocolMetaData > 0 && protocolMetaData <= 15) {
            itemId |= protocolMetaData;
        }
        Item item = getItem(itemId, ProtocolVersion.VERSION_1_12_2);
        if (item == null) {
            // ignore meta data?
            return getItem(protocolId << 4, ProtocolVersion.VERSION_1_12_2);
        }
        return item;
    }

    public static Item getItem(int protocolId, ProtocolVersion version) {
        return itemMap.get(version).get(protocolId);
    }

    public static void load(String mod, JsonObject json, ProtocolVersion version) {
        HashBiMap<Integer, Item> versionMapping = HashBiMap.create();
        for (String identifierName : json.keySet()) {
            Item item = new Item(mod, identifierName);
            itemList.add(item);
            JsonObject identifierJSON = json.getAsJsonObject(identifierName);
            int itemId = identifierJSON.get("id").getAsInt();
            if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
                // old format (with metadata)
                itemId <<= 4;
                if (identifierJSON.has("meta")) {
                    itemId |= identifierJSON.get("meta").getAsInt();
                }
            }
            versionMapping.put(itemId, item);
        }
        itemMap.put(version, versionMapping);
    }

    public static int getItemId(Item item, ProtocolVersion version) {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            version = ProtocolVersion.VERSION_1_12_2;
        }
        int itemId = itemMap.get(version).inverse().get(item);
        if (version.getVersionNumber() == ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return itemId >> 4;
        }
        return itemId;
    }
}
