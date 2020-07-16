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
    static HashMap<ProtocolVersion, HashBiMap<Integer, Block>> blockMap = new HashMap<>(); // version -> (protocolId > block)
    static HashMap<String, HashMap<String, BlockProperties>> propertiesMapping = new HashMap<>();
    static HashMap<String, BlockRotation> rotationMapping = new HashMap<>();

    static {
        HashMap<String, BlockProperties> propertyHashMap;

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 15; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("LEVEL_%d", i)));
        }
        propertiesMapping.put("level", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 5; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("HONEY_LEVEL_%d", i)));
        }
        propertiesMapping.put("honey_level", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 15; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("POWER_%d", i)));
        }
        propertiesMapping.put("power", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 1; i <= 8; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("LAYERS_%d", i)));
        }
        propertiesMapping.put("layers", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 7; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("DISTANCE_%d", i)));
        }
        propertiesMapping.put("distance", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 25; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("AGE_%d", i)));
        }
        propertiesMapping.put("age", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 7; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("DISTANCE_%d", i)));
        }
        propertiesMapping.put("distance", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 7; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("MOISTURE_%d", i)));
        }
        propertiesMapping.put("moisture", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("PICKLES_%d", i)));
        }
        propertiesMapping.put("pickles", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 6; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("BITES_%d", i)));
        }
        propertiesMapping.put("bites", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("DELAY_%d", i)));
        }
        propertiesMapping.put("delay", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 2; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("HATCH_%d", i)));
        }
        propertiesMapping.put("hatch", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("EGGS_%d", i)));
        }
        propertiesMapping.put("eggs", propertyHashMap);

        propertyHashMap = new HashMap<>();
        for (int i = 0; i <= 24; i++) {
            propertyHashMap.put(String.valueOf(i), BlockProperties.valueOf(String.format("NOTE_%d", i)));
        }
        propertiesMapping.put("note", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("0", BlockProperties.STAGE_0);
        propertyHashMap.put("1", BlockProperties.STAGE_1);
        propertiesMapping.put("stage", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.EAST);
        propertyHashMap.put("up", BlockProperties.EAST_UP);
        propertyHashMap.put("side", BlockProperties.EAST_SIDE);
        propertyHashMap.put("false", BlockProperties.NONE);
        propertyHashMap.put("none", BlockProperties.NONE);
        propertiesMapping.put("east", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.WEST);
        propertyHashMap.put("up", BlockProperties.WEST_UP);
        propertyHashMap.put("side", BlockProperties.WEST_SIDE);
        propertyHashMap.put("false", BlockProperties.NONE);
        propertyHashMap.put("none", BlockProperties.NONE);
        propertiesMapping.put("west", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.SOUTH);
        propertyHashMap.put("up", BlockProperties.SOUTH_UP);
        propertyHashMap.put("side", BlockProperties.SOUTH_SIDE);
        propertyHashMap.put("false", BlockProperties.NONE);
        propertyHashMap.put("none", BlockProperties.NONE);
        propertiesMapping.put("south", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.NORTH);
        propertyHashMap.put("up", BlockProperties.NORTH_UP);
        propertyHashMap.put("side", BlockProperties.NORTH_SIDE);
        propertyHashMap.put("false", BlockProperties.NONE);
        propertyHashMap.put("none", BlockProperties.NONE);
        propertiesMapping.put("north", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.SNOWY);
        propertyHashMap.put("false", BlockProperties.NOT_SNOWY);
        propertiesMapping.put("snowy", propertyHashMap);


        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.UP);
        propertyHashMap.put("false", BlockProperties.NOT_UP);
        propertiesMapping.put("up", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.DOWN);
        propertyHashMap.put("false", BlockProperties.NOT_DOWN);
        propertiesMapping.put("down", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.IN_WALL);
        propertyHashMap.put("false", BlockProperties.NOT_IN_WALL);
        propertiesMapping.put("in_wall", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.EXTENDED);
        propertyHashMap.put("false", BlockProperties.NOT_EXTENDED);
        propertiesMapping.put("extended", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.POWERED);
        propertyHashMap.put("false", BlockProperties.NOT_POWERED);
        propertiesMapping.put("powered", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.OPEN);
        propertyHashMap.put("false", BlockProperties.CLOSED);
        propertiesMapping.put("open", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.BOTTOM);
        propertyHashMap.put("false", BlockProperties.NOT_BOTTOM);
        propertiesMapping.put("bottom", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.OCCUPIED);
        propertyHashMap.put("false", BlockProperties.NOT_OCCUPIED);
        propertiesMapping.put("occupied", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.ATTACHED);
        propertyHashMap.put("false", BlockProperties.NOT_ATTACHED);
        propertiesMapping.put("attached", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.DISARMED);
        propertyHashMap.put("false", BlockProperties.ARMED);
        propertiesMapping.put("disarmed", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.INVERTED);
        propertyHashMap.put("false", BlockProperties.NOT_INVERTED);
        propertiesMapping.put("inverted", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.TRIGGERED);
        propertyHashMap.put("false", BlockProperties.NOT_TRIGGERED);
        propertiesMapping.put("triggered", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.CONDITIONAL);
        propertyHashMap.put("false", BlockProperties.UNCONDITIONAL);
        propertiesMapping.put("conditional", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.DRAG);
        propertyHashMap.put("false", BlockProperties.NOT_DRAG);
        propertiesMapping.put("drag", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.UNSTABLE);
        propertyHashMap.put("false", BlockProperties.STABLE);
        propertiesMapping.put("unstable", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.HANGING);
        propertyHashMap.put("false", BlockProperties.NOT_HANGING);
        propertiesMapping.put("hanging", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.HAS_BOOK);
        propertyHashMap.put("false", BlockProperties.NO_BOOK);
        propertiesMapping.put("has_book", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.HAS_BOTTLE_0);
        propertyHashMap.put("false", BlockProperties.NO_BOTTLE_0);
        propertiesMapping.put("has_bottle_0", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.HAS_BOTTLE_1);
        propertyHashMap.put("false", BlockProperties.NO_BOTTLE_1);
        propertiesMapping.put("has_bottle_1", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.HAS_BOTTLE_2);
        propertyHashMap.put("false", BlockProperties.NO_BOTTLE_2);
        propertiesMapping.put("has_bottle_2", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.PERSISTENT);
        propertyHashMap.put("false", BlockProperties.NOT_PERSISTENT);
        propertiesMapping.put("persistent", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.LIT);
        propertyHashMap.put("false", BlockProperties.UN_LIT);
        propertiesMapping.put("lit", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.WATERLOGGED);
        propertyHashMap.put("false", BlockProperties.NOT_WATERLOGGED);
        propertiesMapping.put("waterlogged", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.LOCKED);
        propertyHashMap.put("false", BlockProperties.UNLOCKED);
        propertiesMapping.put("locked", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.EYE);
        propertyHashMap.put("false", BlockProperties.NO_EYE);
        propertiesMapping.put("eye", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.ENABLED);
        propertyHashMap.put("false", BlockProperties.DISABLED);
        propertiesMapping.put("enabled", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.HAS_RECORD);
        propertyHashMap.put("false", BlockProperties.HAS_NO_RECORD);
        propertiesMapping.put("has_record", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.SHORT);
        propertyHashMap.put("false", BlockProperties.LONG);
        propertiesMapping.put("short", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("true", BlockProperties.SIGNAL_FIRE);
        propertyHashMap.put("false", BlockProperties.NOT_SIGNAL_FIRE);
        propertiesMapping.put("signal_fire", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("harp", BlockProperties.HARP);
        propertyHashMap.put("basedrum", BlockProperties.BASEDRUM);
        propertyHashMap.put("snare", BlockProperties.SNARE);
        propertyHashMap.put("hat", BlockProperties.HAT);
        propertyHashMap.put("bass", BlockProperties.BASS);
        propertyHashMap.put("flute", BlockProperties.FLUTE);
        propertyHashMap.put("bell", BlockProperties.BELL);
        propertyHashMap.put("guitar", BlockProperties.GUITAR);
        propertyHashMap.put("chime", BlockProperties.CHIME);
        propertyHashMap.put("xylophone", BlockProperties.XYLOPHONE);
        propertyHashMap.put("iron_xylophone", BlockProperties.IRON_XYLOPHONE);
        propertyHashMap.put("cow_bell", BlockProperties.COW_BELL);
        propertyHashMap.put("didgeridoo", BlockProperties.DIDGERIDOO);
        propertyHashMap.put("bit", BlockProperties.BIT);
        propertyHashMap.put("banjo", BlockProperties.BANJO);
        propertyHashMap.put("pling", BlockProperties.PLING);
        propertiesMapping.put("instrument", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("head", BlockProperties.HEAD);
        propertyHashMap.put("foot", BlockProperties.FOOT);
        propertiesMapping.put("part", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("left", BlockProperties.HINGE_LEFT);
        propertyHashMap.put("right", BlockProperties.HINGE_RIGHT);
        propertiesMapping.put("hinge", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("x", BlockProperties.AXIS_X);
        propertyHashMap.put("y", BlockProperties.AXIS_Y);
        propertyHashMap.put("z", BlockProperties.AXIS_Z);
        propertiesMapping.put("axis", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("floor", BlockProperties.FLOOR);
        propertyHashMap.put("wall", BlockProperties.WALL);
        propertyHashMap.put("ceiling", BlockProperties.CEILING);
        propertiesMapping.put("face", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("floor", BlockProperties.FLOOR);
        propertyHashMap.put("ceiling", BlockProperties.CEILING);
        propertyHashMap.put("single_wall", BlockProperties.SINGLE_WALL);
        propertyHashMap.put("double_wall", BlockProperties.DOUBLE_WALL);
        propertiesMapping.put("attachment", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("save", BlockProperties.SAVE);
        propertyHashMap.put("load", BlockProperties.LOAD);
        propertyHashMap.put("corner", BlockProperties.CORNER);
        propertyHashMap.put("data", BlockProperties.DATA);
        propertyHashMap.put("compare", BlockProperties.COMPARE);
        propertyHashMap.put("subtract", BlockProperties.SUBTRACT);
        propertiesMapping.put("mode", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("top", BlockProperties.HALF_UPPER);
        propertyHashMap.put("upper", BlockProperties.HALF_UPPER);
        propertyHashMap.put("bottom", BlockProperties.HALF_LOWER);
        propertyHashMap.put("lower", BlockProperties.HALF_LOWER);
        propertiesMapping.put("half", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("none", BlockProperties.NONE);
        propertyHashMap.put("small", BlockProperties.LARGE);
        propertyHashMap.put("large", BlockProperties.SMALL);
        propertiesMapping.put("leaves", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("top", BlockProperties.SLAB_TOP);
        propertyHashMap.put("bottom", BlockProperties.SLAB_BOTTOM);
        propertyHashMap.put("double", BlockProperties.SLAB_DOUBLE);
        propertyHashMap.put("normal", BlockProperties.TYPE_NORMAL);
        propertyHashMap.put("sticky", BlockProperties.TYPE_STICKY);
        propertyHashMap.put("single", BlockProperties.TYPE_SINGLE);
        propertyHashMap.put("left", BlockProperties.TYPE_LEFT);
        propertyHashMap.put("right", BlockProperties.TYPE_RIGHT);
        propertiesMapping.put("type", propertyHashMap);

        propertyHashMap = new HashMap<>();
        propertyHashMap.put("straight", BlockProperties.STRAIGHT);
        propertyHashMap.put("inner_left", BlockProperties.INNER_LEFT);
        propertyHashMap.put("inner_right", BlockProperties.INNER_RIGHT);
        propertyHashMap.put("outer_left", BlockProperties.OUTER_LEFT);
        propertyHashMap.put("outer_right", BlockProperties.OUTER_RIGHT);
        propertyHashMap.put("north_south", BlockProperties.NORTH_SOUTH);
        propertyHashMap.put("east_west", BlockProperties.EAST_WEST);
        propertyHashMap.put("south_east", BlockProperties.SOUTH_EAST);
        propertyHashMap.put("south_west", BlockProperties.SOUTH_WEST);
        propertyHashMap.put("north_west", BlockProperties.NORTH_WEST);
        propertyHashMap.put("north_east", BlockProperties.NORTH_EAST);
        propertyHashMap.put("ascending_east", BlockProperties.ASCENDING_EAST);
        propertyHashMap.put("ascending_west", BlockProperties.ASCENDING_WEST);
        propertyHashMap.put("ascending_north", BlockProperties.ASCENDING_NORTH);
        propertyHashMap.put("ascending_south", BlockProperties.ASCENDING_SOUTH);
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
        int blockId = protocolId << 4 | protocolMetaData;
        return getBlock(blockId, ProtocolVersion.VERSION_1_12_2);
    }


    public static Block getBlockByLegacy(int blockIdAndMetaData) {
        return getBlock(blockIdAndMetaData, ProtocolVersion.VERSION_1_12_2);
    }

    public static Block getBlock(int protocolId, ProtocolVersion version) {
        if (version.getVersionNumber() < ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            version = ProtocolVersion.VERSION_1_12_2;
        }
        return blockMap.get(version).get(protocolId);
    }

    public static void load(String mod, JSONObject json, ProtocolVersion version) {
        HashBiMap<Integer, Block> versionMapping = HashBiMap.create();
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
                    BlockProperties[] properties = new BlockProperties[propertiesJSON.length()];
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
        for (Block block : blockList) {
            if (block.getMod().equals(mod) && block.getIdentifier().equals(identifier)) {
                return block;
            }
        }
        return null;
    }

    public static Block getBlock(String mod, String identifier, BlockProperties[] properties, BlockRotation rotation) {
        for (Block block : blockList) {
            if (block.getMod().equals(mod) && block.getIdentifier().equals(identifier) && block.getRotation() == rotation && propertiesEquals(block.getProperties(), properties)) {
                return block;
            }
        }
        return null;
    }

    public static boolean propertiesEquals(BlockProperties[] one, BlockProperties[] two) {
        if (one.length != two.length) {
            return false;
        }
        for (BlockProperties property : one) {
            if (!containsElement(two, property)) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsElement(BlockProperties[] arr, BlockProperties value) {
        for (BlockProperties property : arr) {
            if (property == value) {
                return true;
            }
        }
        return false;
    }


    public static int getBlockId(Block block, ProtocolVersion version) {
        int blockId = blockMap.get(version).inverse().get(block);
        if (version.getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
            return blockId >> 4;
        }
        return blockId;
    }
}
