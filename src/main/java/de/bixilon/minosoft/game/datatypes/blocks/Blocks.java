package de.bixilon.minosoft.game.datatypes.blocks;
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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Blocks {
    public static Block nullBlock;
    static ArrayList<Block> blockList = new ArrayList<>();
    static HashMap<ProtocolVersion, BiMap<Integer, Block>> blockMap = new HashMap<>(); // version -> (protocolId > Item)
    static HashMap<String, HashMap<String, BlockProperty>> propertiesMapping = new HashMap<>();
    static HashMap<String, BlockRotation> rotationMapping = new HashMap<>();

    static {
        HashMap<String, BlockProperty> propertyHashMap;

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 15; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("LEVEL_%d", i)));
        }
        propertiesMapping.put("level", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 5; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("HONEY_LEVEL_%d", i)));
        }
        propertiesMapping.put("honey_level", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 15; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("POWER_%d", i)));
        }
        propertiesMapping.put("power", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 1; i <= 8; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("LAYERS_%d", i)));
        }
        propertiesMapping.put("layers", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 7; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("DISTANCE_%d", i)));
        }
        propertiesMapping.put("distance", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 25; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("AGE_%d", i)));
        }
        propertiesMapping.put("age", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 7; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("DISTANCE_%d", i)));
        }
        propertiesMapping.put("distance", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 7; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("MOISTURE_%d", i)));
        }
        propertiesMapping.put("moisture", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("PICKLES_%d", i)));
        }
        propertiesMapping.put("pickles", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 6; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("BITES_%d", i)));
        }
        propertiesMapping.put("bites", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("DELAY_%d", i)));
        }
        propertiesMapping.put("delay", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 2; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("HATCH_%d", i)));
        }
        propertiesMapping.put("hatch", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("EGGS_%d", i)));
        }
        propertiesMapping.put("eggs", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 24; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperty.valueOf(String.format("NOTE_%d", i)));
        }
        propertiesMapping.put("note", propertyHashMap);


        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.SNOWY);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("snowy", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("0", BlockProperty.STAGE_0);
        propertyHashMap.put("1", BlockProperty.STAGE_1);
        propertiesMapping.put("stage", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.EAST);
        propertyHashMap.put("up", BlockProperty.EAST_UP);
        propertyHashMap.put("side", BlockProperty.EAST_SIDE);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertyHashMap.put("none", BlockProperty.NONE);
        propertiesMapping.put("east", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.WEST);
        propertyHashMap.put("up", BlockProperty.WEST_UP);
        propertyHashMap.put("side", BlockProperty.WEST_SIDE);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertyHashMap.put("none", BlockProperty.NONE);
        propertiesMapping.put("west", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.SOUTH);
        propertyHashMap.put("up", BlockProperty.SOUTH_UP);
        propertyHashMap.put("side", BlockProperty.SOUTH_SIDE);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertyHashMap.put("none", BlockProperty.NONE);
        propertiesMapping.put("south", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.NORTH);
        propertyHashMap.put("up", BlockProperty.NORTH_UP);
        propertyHashMap.put("side", BlockProperty.NORTH_SIDE);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertyHashMap.put("none", BlockProperty.NONE);
        propertiesMapping.put("north", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.UP);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("up", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.DOWN);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("down", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.IN_WALL);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("in_wall", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.EXTENDED);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("extended", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.POWERED);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("powered", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.OPEN);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("open", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.BOTTOM);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("bottom", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.OCCUPIED);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("occupied", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.ATTACHED);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("attached", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.DISARMED);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("disarmed", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.INVERTED);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("inverted", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.TRIGGERED);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("triggered", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.CONDITIONAL);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("conditional", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.DRAG);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("drag", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.UNSTABLE);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("unstable", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.HANGING);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("hanging", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.HAS_BOOK);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("has_book", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.HAS_BOTTLE_0);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("has_bottle_0", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.HAS_BOTTLE_1);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("has_bottle_1", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.HAS_BOTTLE_2);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("has_bottle_2", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.PERSISTENT);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("persistent", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.LIT);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("lit", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.WATERLOGGED);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("waterlogged", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.LOCKED);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("locked", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.EYE);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("eye", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.ENABLED);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("enabled", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.HAS_RECORD);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("has_record", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.SHORT);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("short", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperty.SIGNAL_FIRE);
        propertyHashMap.put("false", BlockProperty.NONE);
        propertiesMapping.put("signal_fire", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("harp", BlockProperty.HARP);
        propertyHashMap.put("basedrum", BlockProperty.BASEDRUM);
        propertyHashMap.put("snare", BlockProperty.SNARE);
        propertyHashMap.put("hat", BlockProperty.HAT);
        propertyHashMap.put("bass", BlockProperty.BASS);
        propertyHashMap.put("flute", BlockProperty.FLUTE);
        propertyHashMap.put("bell", BlockProperty.BELL);
        propertyHashMap.put("guitar", BlockProperty.GUITAR);
        propertyHashMap.put("chime", BlockProperty.CHIME);
        propertyHashMap.put("xylophone", BlockProperty.XYLOPHONE);
        propertyHashMap.put("iron_xylophone", BlockProperty.IRON_XYLOPHONE);
        propertyHashMap.put("cow_bell", BlockProperty.COW_BELL);
        propertyHashMap.put("didgeridoo", BlockProperty.DIDGERIDOO);
        propertyHashMap.put("bit", BlockProperty.BIT);
        propertyHashMap.put("banjo", BlockProperty.BANJO);
        propertyHashMap.put("pling", BlockProperty.PLING);
        propertiesMapping.put("instrument", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("head", BlockProperty.HEAD);
        propertyHashMap.put("foot", BlockProperty.FOOT);
        propertiesMapping.put("part", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("left", BlockProperty.HINGE_LEFT);
        propertyHashMap.put("right", BlockProperty.HINGE_RIGHT);
        propertiesMapping.put("hinge", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("x", BlockProperty.AXIS_X);
        propertyHashMap.put("y", BlockProperty.AXIS_Y);
        propertyHashMap.put("z", BlockProperty.AXIS_Z);
        propertiesMapping.put("axis", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("floor", BlockProperty.FLOOR);
        propertyHashMap.put("wall", BlockProperty.WALL);
        propertyHashMap.put("ceiling", BlockProperty.CEILING);
        propertiesMapping.put("face", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("floor", BlockProperty.FLOOR);
        propertyHashMap.put("ceiling", BlockProperty.CEILING);
        propertyHashMap.put("single_wall", BlockProperty.SINGLE_WALL);
        propertyHashMap.put("double_wall", BlockProperty.DOUBLE_WALL);
        propertiesMapping.put("attachment", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("save", BlockProperty.SAVE);
        propertyHashMap.put("load", BlockProperty.LOAD);
        propertyHashMap.put("corner", BlockProperty.CORNER);
        propertyHashMap.put("data", BlockProperty.DATA);
        propertyHashMap.put("compare", BlockProperty.COMPARE);
        propertyHashMap.put("subtract", BlockProperty.SUBTRACT);
        propertiesMapping.put("mode", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("top", BlockProperty.HALF_UPPER);
        propertyHashMap.put("upper", BlockProperty.HALF_UPPER);
        propertyHashMap.put("bottom", BlockProperty.HALF_LOWER);
        propertyHashMap.put("lower", BlockProperty.HALF_LOWER);
        propertiesMapping.put("half", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("none", BlockProperty.NONE);
        propertyHashMap.put("small", BlockProperty.LARGE);
        propertyHashMap.put("large", BlockProperty.SMALL);
        propertiesMapping.put("leaves", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("top", BlockProperty.SLAB_TOP);
        propertyHashMap.put("bottom", BlockProperty.SLAB_BOTTOM);
        propertyHashMap.put("double", BlockProperty.SLAB_DOUBLE);
        propertyHashMap.put("normal", BlockProperty.TYPE_NORMAL);
        propertyHashMap.put("sticky", BlockProperty.TYPE_STICKY);
        propertyHashMap.put("single", BlockProperty.TYPE_SINGLE);
        propertyHashMap.put("left", BlockProperty.TYPE_LEFT);
        propertyHashMap.put("right", BlockProperty.TYPE_RIGHT);
        propertiesMapping.put("type", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("straight", BlockProperty.STRAIGHT);
        propertyHashMap.put("inner_left", BlockProperty.INNER_LEFT);
        propertyHashMap.put("inner_right", BlockProperty.INNER_RIGHT);
        propertyHashMap.put("outer_left", BlockProperty.OUTER_LEFT);
        propertyHashMap.put("outer_right", BlockProperty.OUTER_RIGHT);
        propertyHashMap.put("north_south", BlockProperty.NORTH_SOUTH);
        propertyHashMap.put("east_west", BlockProperty.EAST_WEST);
        propertyHashMap.put("south_east", BlockProperty.SOUTH_EAST);
        propertyHashMap.put("south_west", BlockProperty.SOUTH_WEST);
        propertyHashMap.put("north_west", BlockProperty.NORTH_WEST);
        propertyHashMap.put("north_east", BlockProperty.NORTH_EAST);
        propertyHashMap.put("ascending_east", BlockProperty.ASCENDING_EAST);
        propertyHashMap.put("ascending_west", BlockProperty.ASCENDING_WEST);
        propertyHashMap.put("ascending_north", BlockProperty.ASCENDING_NORTH);
        propertyHashMap.put("ascending_south", BlockProperty.ASCENDING_SOUTH);
        propertiesMapping.put("shape", propertyHashMap);

        rotationMapping.put("0", BlockRotation.SOUTH);
        rotationMapping.put("1", BlockRotation.SOUTH_SOUTH_WEST);
        rotationMapping.put("2", BlockRotation.SOUTH_WEST);
        rotationMapping.put("3", BlockRotation.WEST_SOUTH_WEST);
        rotationMapping.put("4", BlockRotation.WEST);
        rotationMapping.put("5", BlockRotation.WEST_NORTH_WEST);
        rotationMapping.put("6", BlockRotation.NORTH_WEST);
        rotationMapping.put("7", BlockRotation.NORTH_NORTH_WEST);
        rotationMapping.put("8", BlockRotation.NORTH);
        rotationMapping.put("9", BlockRotation.NORTH_NORTH_EAST);
        rotationMapping.put("10", BlockRotation.NORTH_EAST);
        rotationMapping.put("11", BlockRotation.EAST_NORTH_EAST);
        rotationMapping.put("12", BlockRotation.EAST);
        rotationMapping.put("13", BlockRotation.EAST_SOUTH_EAST);
        rotationMapping.put("14", BlockRotation.SOUTH_EAST);
        rotationMapping.put("15", BlockRotation.SOUTH_SOUTH_EAST);
        rotationMapping.put("south", BlockRotation.SOUTH);
        rotationMapping.put("east", BlockRotation.EAST);
        rotationMapping.put("north", BlockRotation.NONE);
        rotationMapping.put("west", BlockRotation.WEST);
        rotationMapping.put("up", BlockRotation.UP);
        rotationMapping.put("down", BlockRotation.DOWN);
        rotationMapping.put("ascending_east", BlockRotation.ASCENDING_EAST);
        rotationMapping.put("ascending_west", BlockRotation.ASCENDING_WEST);
        rotationMapping.put("ascending_north", BlockRotation.ASCENDING_NORTH);
        rotationMapping.put("ascending_south", BlockRotation.ASCENDING_SOUTH);
        rotationMapping.put("north_south", BlockRotation.NORTH_SOUTH);
        rotationMapping.put("east_west", BlockRotation.EAST_WEST);


    }

    public static Block getBlockByLegacy(int protocolId, int protocolMetaData) {
        int itemId = protocolId << 4 | protocolMetaData;
        return getBlock(itemId, ProtocolVersion.VERSION_1_12_2);
    }


    public static Block getBlockByLegacy(int itemIdAndMetaData) {
        return getBlock(itemIdAndMetaData, ProtocolVersion.VERSION_1_12_2);
    }

    public static Block getBlock(int protocolId, ProtocolVersion version) {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            version = ProtocolVersion.VERSION_1_12_2;
        }
        return blockMap.get(version).get(protocolId);
    }

    public static void load(String mod, JSONObject json, ProtocolVersion version) {
        BiMap<Integer, Block> versionMapping = HashBiMap.create();
        for (Iterator<String> identifiers = json.keys(); identifiers.hasNext(); ) {
            String identifierName = identifiers.next();
            JSONObject identifierJSON = json.getJSONObject(identifierName);
            JSONArray statesArray = identifierJSON.getJSONArray("states");
            for (int i = 0; i < statesArray.length(); i++) {
                JSONObject statesJSON = statesArray.getJSONObject(i);
                if (statesJSON.has("properties")) {
                    // properties are optional
                    JSONObject propertiesJSON = statesJSON.getJSONObject("properties");
                    BlockRotation rotation = BlockRotation.NONE;
                    if (propertiesJSON.has("facing")) {
                        rotation = rotationMapping.get(propertiesJSON.getString("facing"));
                        propertiesJSON.remove("facing");

                    } else if (propertiesJSON.has("rotation")) {
                        rotation = rotationMapping.get(propertiesJSON.getString("rotation"));
                        propertiesJSON.remove("rotation");
                    }
                    BlockProperty[] properties = new BlockProperty[propertiesJSON.length()];
                    int ii = 0;
                    for (Iterator<String> it = propertiesJSON.keys(); it.hasNext(); ) {
                        String propertyName = it.next();
                        if (propertiesMapping.get(propertyName) == null) {
                            throw new RuntimeException(String.format("Unknown block property: %s (identifier=%s)", propertyName, identifierName));
                        }
                        if (propertiesMapping.get(propertyName).get(propertiesJSON.getString(propertyName)) == null) {
                            throw new RuntimeException(String.format("Unknown block property: %s -> %s (identifier=%s)", propertyName, propertiesJSON.getString(propertyName), identifierName));
                        }
                        properties[ii] = propertiesMapping.get(propertyName).get(propertiesJSON.getString(propertyName));
                        ii++;
                    }

                    Block block = getBlock(mod, identifierName, properties, rotation);

                    if (block == null) {
                        // does not exist. create
                        block = new Block(mod, identifierName, properties, rotation);
                        blockList.add(block);
                    }

                    versionMapping.put(getBlockId(statesJSON, version), block);
                } else {
                    // no properties, directly add block
                    Block block = getBlock(mod, identifierName);

                    if (block == null) {
                        // does not exist. create
                        block = new Block(mod, identifierName);
                        blockList.add(block);
                    }

                    versionMapping.put(getBlockId(statesJSON, version), block);
                }
            }
        }
        blockMap.put(version, versionMapping);
    }

    private static int getBlockId(JSONObject json, ProtocolVersion version) {
        int blockId = json.getInt("id");
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            // old format (with metadata)
            blockId <<= 4;
            if (json.has("meta")) {
                blockId |= json.getInt("meta");
            }
        }
        return blockId;
    }

    public static Block getBlock(String mod, String identifier) {
        for (Block item : blockList) {
            if (item.getMod().equals(mod) && item.getIdentifier().equals(identifier)) {
                return item;
            }
        }
        return null;
    }

    public static Block getBlock(String mod, String identifier, BlockProperty[] properties, BlockRotation rotation) {
        for (Block block : blockList) {
            if (block.getMod().equals(mod) && block.getIdentifier().equals(identifier) && block.getRotation() == rotation && propertiesEquals(block.getProperties(), properties)) {
                return block;
            }
        }
        return null;
    }

    public static boolean propertiesEquals(BlockProperty[] one, BlockProperty[] two) {
        if (one.length != two.length) {
            return false;
        }
        for (BlockProperty property : one) {
            if (!containsElement(two, property)) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsElement(BlockProperty[] arr, BlockProperty value) {
        for (BlockProperty property : arr) {
            if (property == value)
                return true;
        }
        return false;
    }


    public static int getBlockId(Block item, ProtocolVersion version) {
        int itemId = blockMap.get(version).inverse().get(item);
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return itemId >> 4;
        }
        return itemId;
    }
}
