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

package de.bixilon.minosoft.data.mappings.blocks;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.config.StaticConfiguration;

import java.util.HashSet;

public class Blocks {
    public static final Block nullBlock = new Block("air");

    public static HashBiMap<Integer, Block> load(String mod, JsonObject json, boolean metaData) {
        HashBiMap<Integer, Block> versionMapping = HashBiMap.create();
        for (String identifierName : json.keySet()) {
            JsonObject identifierJSON = json.getAsJsonObject(identifierName);
            JsonArray statesArray = identifierJSON.getAsJsonArray("states");
            for (int i = 0; i < statesArray.size(); i++) {
                JsonObject statesJSON = statesArray.get(i).getAsJsonObject();
                Block block;
                if (statesJSON.has("properties")) {
                    // properties are optional
                    JsonObject propertiesJSON = statesJSON.getAsJsonObject("properties");
                    BlockRotations rotation = BlockRotations.NONE;
                    if (propertiesJSON.has("facing")) {
                        rotation = BlockRotations.ROTATION_MAPPING.get(propertiesJSON.get("facing").getAsString());
                        propertiesJSON.remove("facing");
                    } else if (propertiesJSON.has("rotation")) {
                        rotation = BlockRotations.ROTATION_MAPPING.get(propertiesJSON.get("rotation").getAsString());
                        propertiesJSON.remove("rotation");
                    } else if (propertiesJSON.has("orientation")) {
                        rotation = BlockRotations.ROTATION_MAPPING.get(propertiesJSON.get("orientation").getAsString());
                        propertiesJSON.remove("orientation");
                    } else if (propertiesJSON.has("axis")) {
                        rotation = BlockRotations.ROTATION_MAPPING.get(propertiesJSON.get("axis").getAsString());
                        propertiesJSON.remove("axis");
                    }

                    HashSet<BlockProperties> properties = new HashSet<>();
                    for (String propertyName : propertiesJSON.keySet()) {
                        if (StaticConfiguration.DEBUG_MODE) {
                            if (BlockProperties.PROPERTIES_MAPPING.get(propertyName) == null) {
                                throw new RuntimeException(String.format("Unknown block property: %s (identifier=%s)", propertyName, identifierName));
                            }
                            if (BlockProperties.PROPERTIES_MAPPING.get(propertyName).get(propertiesJSON.get(propertyName).getAsString()) == null) {
                                throw new RuntimeException(String.format("Unknown block property: %s -> %s (identifier=%s)", propertyName, propertiesJSON.get(propertyName).getAsString(), identifierName));
                            }
                        }
                        properties.add(BlockProperties.PROPERTIES_MAPPING.get(propertyName).get(propertiesJSON.get(propertyName).getAsString()));
                    }

                    block = new Block(mod, identifierName, properties, rotation);
                } else {
                    // no properties, directly add block
                    block = new Block(mod, identifierName);
                }
                int blockId = getBlockId(statesJSON, metaData);
                if (StaticConfiguration.DEBUG_MODE) {
                    checkAndCrashIfBlockIsIn(blockId, identifierName, versionMapping);
                }
                versionMapping.put(blockId, block);
            }
        }
        return versionMapping;
    }

    private static int getBlockId(JsonObject json, boolean metaData) {
        int blockId = json.get("id").getAsInt();
        if (metaData) {
            blockId <<= 4;
            if (json.has("meta")) {
                // old format (with metadata)
                blockId |= json.get("meta").getAsByte();
            }
        }
        return blockId;
    }

    private static void checkAndCrashIfBlockIsIn(int blockId, String identifierName, HashBiMap<Integer, Block> versionMapping) {
        if (versionMapping.containsKey(blockId)) {
            throw new RuntimeException(String.format("Block Id %s is already present for %s! (identifier=%s)", blockId, versionMapping.get(blockId), identifierName));
        }
    }
}
