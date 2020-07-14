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

package de.bixilon.minosoft.game.datatypes.entities.items;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Items {

    static ArrayList<Item> itemList = new ArrayList<>();
    static HashMap<ProtocolVersion, BiMap<Integer, Item>> itemMap = new HashMap<>(); // version -> (protocolId > Item)

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

    public static void load(String mod, JSONObject json, ProtocolVersion version) {
        BiMap<Integer, Item> versionMapping = HashBiMap.create();
        for (Iterator<String> identifiers = json.keys(); identifiers.hasNext(); ) {
            String identifierName = identifiers.next();
            Item item = getItem(mod, identifierName);
            if (item == null) {
                // does not exist. create
                item = new Item(mod, identifierName);
                itemList.add(item);
            }
            JSONObject identifierJSON = json.getJSONObject(identifierName);
            int itemId = identifierJSON.getInt("id");
            if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
                // old format (with metadata)
                itemId <<= 4;
                if (identifierJSON.has("meta")) {
                    itemId |= identifierJSON.getInt("meta");
                }
            }
            versionMapping.put(itemId, item);
        }
        itemMap.put(version, versionMapping);
    }

    public static Item getItem(String mod, String identifier) {
        for (Item item : itemList) {
            if (item.getMod().equals(mod) && item.getIdentifier().equals(identifier)) {
                return item;
            }
        }
        return null;
    }

    public static int getItemId(Item item, ProtocolVersion version) {
        int itemId = itemMap.get(version).inverse().get(item);
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return itemId >> 4;
        }
        return itemId;
    }

}
