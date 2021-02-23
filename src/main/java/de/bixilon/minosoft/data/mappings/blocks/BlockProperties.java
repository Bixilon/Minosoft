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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public enum BlockProperties {
    // rails, doors, daylight sensor, ...
    REDSTONE_POWERED_YES,
    REDSTONE_POWERED_NO,
    REDSTONE_INVERTED_YES,
    REDSTONE_INVERTED_NO,

    // furnace, candles, redstone torches, ...
    GENERAL_LIT_YES,
    GENERAL_LIT_NO,

    // sign, fence, trapdoors, stairs, ...
    GENERAL_WATERLOGGED_YES,
    GENERAL_WATERLOGGED_NO,

    // stairs
    STAIR_DIRECTIONAL_STRAIGHT("shape", "straight"),
    STAIR_DIRECTIONAL_INNER_LEFT("shape", "inner_left"),
    STAIR_DIRECTIONAL_INNER_RIGHT("shape", "inner_right"),
    STAIR_DIRECTIONAL_OUTER_LEFT("shape", "outer_left"),
    STAIR_DIRECTIONAL_OUTER_RIGHT("shape", "outer_right"),
    STAIR_HALF_TOP,
    STAIR_HALF_BOTTOM,

    // slabs
    SLAB_TYPE_TOP,
    SLAB_TYPE_BOTTOM,
    SLAB_TYPE_DOUBLE,

    // farmland
    FARMLAND_MOISTURE_LEVEL_0,
    FARMLAND_MOISTURE_LEVEL_1,
    FARMLAND_MOISTURE_LEVEL_2,
    FARMLAND_MOISTURE_LEVEL_3,
    FARMLAND_MOISTURE_LEVEL_4,
    FARMLAND_MOISTURE_LEVEL_5,
    FARMLAND_MOISTURE_LEVEL_6,
    FARMLAND_MOISTURE_LEVEL_7,

    // plants, stairs
    PLANT_HALF_UPPER,
    PLANT_HALF_LOWER,

    // fluids
    FLUID_LEVEL_0("level"),
    FLUID_LEVEL_1("level"),
    FLUID_LEVEL_2("level"),
    FLUID_LEVEL_3("level"),
    FLUID_LEVEL_4("level"),
    FLUID_LEVEL_5("level"),
    FLUID_LEVEL_6("level"),
    FLUID_LEVEL_7("level"),
    FLUID_LEVEL_8("level"),
    FLUID_LEVEL_9("level"),
    FLUID_LEVEL_10("level"),
    FLUID_LEVEL_11("level"),
    FLUID_LEVEL_12("level"),
    FLUID_LEVEL_13("level"),
    FLUID_LEVEL_14("level"),
    FLUID_LEVEL_15("level"),

    // bee hive
    HONEY_LEVEL_0("honey_level"),
    HONEY_LEVEL_1("honey_level"),
    HONEY_LEVEL_2("honey_level"),
    HONEY_LEVEL_3("honey_level"),
    HONEY_LEVEL_4("honey_level"),
    HONEY_LEVEL_5("honey_level"),

    // pistons
    PISTON_EXTENDED_YES,
    PISTON_EXTENDED_NO,

    // piston head
    PISTON_TYPE_NORMAL,
    PISTON_TYPE_STICKY,
    PISTON_SHORT_YES,
    PISTON_SHORT_NO,

    // rails
    RAILS_DIRECTION_NORTH_SOUTH("shape", "north_south"),
    RAILS_DIRECTION_SOUTH_EAST("shape", "south_east"),
    RAILS_DIRECTION_SOUTH_WEST("shape", "south_west"),
    RAILS_DIRECTION_NORTH_WEST("shape", "north_west"),
    RAILS_DIRECTION_NORTH_EAST("shape", "north_east"),
    RAILS_DIRECTION_EAST_WEST("shape", "east_west"),
    RAILS_DIRECTION_ASCENDING_EAST("shape", "ascending_east"),
    RAILS_DIRECTION_ASCENDING_WEST("shape", "ascending_west"),
    RAILS_DIRECTION_ASCENDING_NORTH("shape", "ascending_north"),
    RAILS_DIRECTION_ASCENDING_SOUTH("shape", "ascending_south"),

    // grass, mycelium
    GRASS_SNOWY_YES,
    GRASS_SNOWY_NO,

    // bamboo, sapling, plants
    PLANTS_STAGE_LEVEL_0,
    PLANTS_STAGE_LEVEL_1,

    // dispenser
    DISPENSER_TRIGGERED_YES,
    DISPENSER_TRIGGERED_NO,

    // leaves
    LEAVES_DISTANCE_LEVEL_0,
    LEAVES_DISTANCE_LEVEL_1,
    LEAVES_DISTANCE_LEVEL_2,
    LEAVES_DISTANCE_LEVEL_3,
    LEAVES_DISTANCE_LEVEL_4,
    LEAVES_DISTANCE_LEVEL_5,
    LEAVES_DISTANCE_LEVEL_6,
    LEAVES_DISTANCE_LEVEL_7,
    LEAVES_PERSISTENT_YES,
    LEAVES_PERSISTENT_NO,

    // bed
    BED_PART_HEAD,
    BED_PART_FOOT,
    BED_OCCUPIED_YES,
    BED_OCCUPIED_NO,

    // tnt
    TNT_UNSTABLE_YES,
    TNT_UNSTABLE_NO,

    // door
    DOOR_HINGE_LEFT,
    DOOR_HINGE_RIGHT,
    DOOR_OPEN_YES,
    DOOR_OPEN_NO,

    // fire
    FIRE_POSITION_NORTH_YES,
    FIRE_POSITION_NORTH_NO,
    FIRE_POSITION_SOUTH_YES,
    FIRE_POSITION_SOUTH_NO,
    FIRE_POSITION_EAST_YES,
    FIRE_POSITION_EAST_NO,
    FIRE_POSITION_WEST_YES,
    FIRE_POSITION_WEST_NO,
    FIRE_POSITION_UP_YES,
    FIRE_POSITION_UP_NO,
    FIRE_POSITION_DOWN_YES,
    FIRE_POSITION_DOWN_NO,
    FIRE_AGE_LEVEL_0,
    FIRE_AGE_LEVEL_1,
    FIRE_AGE_LEVEL_2,
    FIRE_AGE_LEVEL_3,
    FIRE_AGE_LEVEL_4,
    FIRE_AGE_LEVEL_5,
    FIRE_AGE_LEVEL_6,
    FIRE_AGE_LEVEL_7,
    FIRE_AGE_LEVEL_8,
    FIRE_AGE_LEVEL_9,
    FIRE_AGE_LEVEL_10,
    FIRE_AGE_LEVEL_11,
    FIRE_AGE_LEVEL_12,
    FIRE_AGE_LEVEL_13,
    FIRE_AGE_LEVEL_14,
    FIRE_AGE_LEVEL_15,
    FIRE_AGE_LEVEL_16,
    FIRE_AGE_LEVEL_17,
    FIRE_AGE_LEVEL_18,
    FIRE_AGE_LEVEL_19,
    FIRE_AGE_LEVEL_20,
    FIRE_AGE_LEVEL_21,
    FIRE_AGE_LEVEL_22,
    FIRE_AGE_LEVEL_23,
    FIRE_AGE_LEVEL_24,
    FIRE_AGE_LEVEL_25,

    // noteblock
    NOTEBLOCK_INSTRUMENT_HARP,
    NOTEBLOCK_INSTRUMENT_BASE_DRUM("instrument", "basedrum"),
    NOTEBLOCK_INSTRUMENT_SNARE,
    NOTEBLOCK_INSTRUMENT_HAT,
    NOTEBLOCK_INSTRUMENT_BASS,
    NOTEBLOCK_INSTRUMENT_FLUTE,
    NOTEBLOCK_INSTRUMENT_BELL,
    NOTEBLOCK_INSTRUMENT_GUITAR,
    NOTEBLOCK_INSTRUMENT_CHIME,
    NOTEBLOCK_INSTRUMENT_XYLOPHONE,
    NOTEBLOCK_INSTRUMENT_IRON_XYLOPHONE("instrument", "iron_xylophone"),
    NOTEBLOCK_INSTRUMENT_COW_BELL("instrument", "cow_bell"),
    NOTEBLOCK_INSTRUMENT_DIDGERIDOO,
    NOTEBLOCK_INSTRUMENT_BIT,
    NOTEBLOCK_INSTRUMENT_BANJO,
    NOTEBLOCK_INSTRUMENT_PLING,

    NOTEBLOCK_NOTE_LEVEL_0,
    NOTEBLOCK_NOTE_LEVEL_1,
    NOTEBLOCK_NOTE_LEVEL_2,
    NOTEBLOCK_NOTE_LEVEL_3,
    NOTEBLOCK_NOTE_LEVEL_4,
    NOTEBLOCK_NOTE_LEVEL_5,
    NOTEBLOCK_NOTE_LEVEL_6,
    NOTEBLOCK_NOTE_LEVEL_7,
    NOTEBLOCK_NOTE_LEVEL_8,
    NOTEBLOCK_NOTE_LEVEL_9,
    NOTEBLOCK_NOTE_LEVEL_10,
    NOTEBLOCK_NOTE_LEVEL_11,
    NOTEBLOCK_NOTE_LEVEL_12,
    NOTEBLOCK_NOTE_LEVEL_13,
    NOTEBLOCK_NOTE_LEVEL_14,
    NOTEBLOCK_NOTE_LEVEL_15,
    NOTEBLOCK_NOTE_LEVEL_16,
    NOTEBLOCK_NOTE_LEVEL_17,
    NOTEBLOCK_NOTE_LEVEL_18,
    NOTEBLOCK_NOTE_LEVEL_19,
    NOTEBLOCK_NOTE_LEVEL_20,
    NOTEBLOCK_NOTE_LEVEL_21,
    NOTEBLOCK_NOTE_LEVEL_22,
    NOTEBLOCK_NOTE_LEVEL_23,
    NOTEBLOCK_NOTE_LEVEL_24,

    // redstone
    REDSTONE_POWER_0,
    REDSTONE_POWER_1,
    REDSTONE_POWER_2,
    REDSTONE_POWER_3,
    REDSTONE_POWER_4,
    REDSTONE_POWER_5,
    REDSTONE_POWER_6,
    REDSTONE_POWER_7,
    REDSTONE_POWER_8,
    REDSTONE_POWER_9,
    REDSTONE_POWER_10,
    REDSTONE_POWER_11,
    REDSTONE_POWER_12,
    REDSTONE_POWER_13,
    REDSTONE_POWER_14,
    REDSTONE_POWER_15,
    REDSTONE_POSITION_NORTH_NONE("north", "none"),
    REDSTONE_POSITION_NORTH_LOW("north", "low"),
    REDSTONE_POSITION_NORTH_UP("north", "up"),
    REDSTONE_POSITION_NORTH_SIDE("north", "side"),
    REDSTONE_POSITION_NORTH_TALL("north", "tall"),
    REDSTONE_POSITION_WEST_NONE("west", "none"),
    REDSTONE_POSITION_WEST_LOW("west", "low"),
    REDSTONE_POSITION_WEST_UP("west", "up"),
    REDSTONE_POSITION_WEST_SIDE("west", "side"),
    REDSTONE_POSITION_WEST_TALL("west", "tall"),
    REDSTONE_POSITION_SOUTH_NONE("south", "none"),
    REDSTONE_POSITION_SOUTH_LOW("south", "low"),
    REDSTONE_POSITION_SOUTH_UP("south", "up"),
    REDSTONE_POSITION_SOUTH_SIDE("south", "side"),
    REDSTONE_POSITION_SOUTH_TALL("south", "tall"),
    REDSTONE_POSITION_EAST_NONE("east", "none"),
    REDSTONE_POSITION_EAST_LOW("east", "low"),
    REDSTONE_POSITION_EAST_UP("east", "up"),
    REDSTONE_POSITION_EAST_SIDE("east", "side"),
    REDSTONE_POSITION_EAST_TALL("east", "tall"),

    // snow
    SNOW_LAYERS_LEVEL_1,
    SNOW_LAYERS_LEVEL_2,
    SNOW_LAYERS_LEVEL_3,
    SNOW_LAYERS_LEVEL_4,
    SNOW_LAYERS_LEVEL_5,
    SNOW_LAYERS_LEVEL_6,
    SNOW_LAYERS_LEVEL_7,
    SNOW_LAYERS_LEVEL_8,

    // fence
    FENCE_IN_WALL_YES("in_wall"),
    FENCE_IN_WALL_NO("in_wall"),

    // scaffolding
    SCAFFOLDING_BOTTOM_YES,
    SCAFFOLDING_BOTTOM_NO,

    // tripwire
    TRIPWIRE_DISARMED_YES,
    TRIPWIRE_DISARMED_NO,
    TRIPWIRE_IN_AIR_YES("in_air"),
    TRIPWIRE_IN_AIR_NO("in_air"),

    // tripwire hook
    TRIPWIRE_ATTACHED_YES,
    TRIPWIRE_ATTACHED_NO,

    // structure block, comparator
    STRUCTURE_BLOCK_MODE_SAVE("mode", "save"),
    STRUCTURE_BLOCK_MODE_LOAD("mode", "load"),
    STRUCTURE_BLOCK_MODE_CORNER("mode", "corner"),
    STRUCTURE_BLOCK_MODE_DATA("mode", "data"),
    STRUCTURE_BLOCK_MODE_COMPARE("mode", "compare"),
    STRUCTURE_BLOCK_MODE_SUBTRACT("mode", "subtract"),

    // command block
    COMMAND_BLOCK_CONDITIONAL_YES,
    COMMAND_BLOCK_CONDITIONAL_NO,

    // double column
    BUBBLE_COLUMN_DRAG_YES("drag"), // whirlpool
    BUBBLE_COLUMN_DRAG_NO("drag"), // upwards

    // bell
    BELL_ATTACHMENT_FLOOR,
    BELL_ATTACHMENT_CEILING,
    BELL_ATTACHMENT_SINGLE_WALL("attachment", "single_wall"),
    BELL_ATTACHMENT_DOUBLE_WALL("attachment", "double_wall"),

    // lantern
    LANTERN_HANGING_YES,
    LANTERN_HANGING_NO,

    // sea pickle
    SEA_PICKLE_PICKLES_LEVEL_1,
    SEA_PICKLE_PICKLES_LEVEL_2,
    SEA_PICKLE_PICKLES_LEVEL_3,
    SEA_PICKLE_PICKLES_LEVEL_4,

    // lectern
    LECTERN_BOOK_YES("has_book"),
    LECTERN_BOOK_NO("has_book"),

    // brewing stand
    BREWING_STAND_BOTTLE_0_YES("has_bottle_0"),
    BREWING_STAND_BOTTLE_0_NO("has_bottle_0"),
    BREWING_STAND_BOTTLE_1_YES("has_bottle_1"),
    BREWING_STAND_BOTTLE_1_NO("has_bottle_1"),
    BREWING_STAND_BOTTLE_2_YES("has_bottle_2"),
    BREWING_STAND_BOTTLE_2_NO("has_bottle_2"),

    // chest
    CHEST_TYPE_SINGLE,
    CHEST_TYPE_LEFT,
    CHEST_TYPE_RIGHT,

    // cake
    CAKES_BITES_LEVEL_0,
    CAKES_BITES_LEVEL_1,
    CAKES_BITES_LEVEL_2,
    CAKES_BITES_LEVEL_3,
    CAKES_BITES_LEVEL_4,
    CAKES_BITES_LEVEL_5,
    CAKES_BITES_LEVEL_6,

    // bamboo
    BAMBOO_LEAVES_NONE,
    BAMBOO_LEAVES_SMALL,
    BAMBOO_LEAVES_LARGE,

    // repeater
    REPEATER_LOCKED_YES,
    REPEATER_LOCKED_NO,
    REPEATER_DELAY_LEVEL_1,
    REPEATER_DELAY_LEVEL_2,
    REPEATER_DELAY_LEVEL_3,
    REPEATER_DELAY_LEVEL_4,

    // end portal frame
    PORTAL_FRAME_EYE_YES,
    PORTAL_FRAME_EYE_NO,

    // jukebox
    JUKEBOX_HAS_RECORD_YES("has_record"),
    JUKEBOX_HAS_RECORD_NO("has_record"),

    // campfire
    CAMPFIRE_SIGNAL_FIRE_YES("signal_fire"),
    CAMPFIRE_SIGNAL_FIRE_NO("signal_fire"),

    // turtle eggs
    TURTLE_EGGS_EGGS_LEVEL_1("eggs"),
    TURTLE_EGGS_EGGS_LEVEL_2("eggs"),
    TURTLE_EGGS_EGGS_LEVEL_3("eggs"),
    TURTLE_EGGS_EGGS_LEVEL_4("eggs"),
    TURTLE_EGGS_HATCH_LEVEL_0("hatch"),
    TURTLE_EGGS_HATCH_LEVEL_1("hatch"),
    TURTLE_EGGS_HATCH_LEVEL_2("hatch"),

    // respawn anchor
    RESPAWN_ANCHOR_CHARGES_LEVEL_0,
    RESPAWN_ANCHOR_CHARGES_LEVEL_1,
    RESPAWN_ANCHOR_CHARGES_LEVEL_2,
    RESPAWN_ANCHOR_CHARGES_LEVEL_3,
    RESPAWN_ANCHOR_CHARGES_LEVEL_4,

    // candles
    CANDLE_CANDLES_LEVEL_1,
    CANDLE_CANDLES_LEVEL_2,
    CANDLE_CANDLES_LEVEL_3,
    CANDLE_CANDLES_LEVEL_4,

    // grindstone
    GRINDSTONE_FACE_FLOOR,
    GRINDSTONE_FACE_WALL,
    GRINDSTONE_FACE_CEILING,

    // hopper
    HOPPER_ENABLED_YES,
    HOPPER_ENABLED_NO,

    // button
    BUTTON_FACE_FLOOR,
    BUTTON_FACE_WALL,
    BUTTON_FACE_CEILING,

    POINTED_DRIPSTONE_THICKNESS_TIP_MERGE("thickness", "tip_merge"),
    POINTED_DRIPSTONE_THICKNESS_TIP("thickness", "tip"),
    POINTED_DRIPSTONE_THICKNESS_FRUSTUM("thickness", "frustum"),
    POINTED_DRIPSTONE_THICKNESS_MIDDLE("thickness", "middle"),
    POINTED_DRIPSTONE_THICKNESS_BASE("thickness", "base"),
    POINTED_DRIPSTONE_VERTICAL_DIRECTION_UP("vertical_direction", "up"),
    POINTED_DRIPSTONE_VERTICAL_DIRECTION_DOWN("vertical_direction", "down"),

    LEGACY_LEAVES_BLOCK_UPDATE_YES("block_update"),
    LEGACY_LEAVES_BLOCK_UPDATE_NO("block_update"),

    // is smooth cobble stone in < 17w46a a thing? probably (double cobble stone slab is smooth so ... (and even more))
    LEGACY_SMOOTH_YES,
    LEGACY_SMOOTH_NO,

    SCULK_SENSOR_PHASE_INACTIVE("sculk_sensor_phase", "inactive"),
    SCULK_SENSOR_PHASE_ACTIVE("sculk_sensor_phase", "active"),
    SCULK_SENSOR_PHASE_COOLDOWN("sculk_sensor_phase", "cooldown"),

    DRIPSTONE_TILT_NONE,
    DRIPSTONE_TILT_UNSTABLE,
    DRIPSTONE_TILT_PARTIAL,
    DRIPSTONE_TILT_FULL,

    CAVE_VINES_BERRIES_YES,
    CAVE_VINES_BERRIES_NO,
    ;

    public static final HashMap<String, HashMap<Object, BlockProperties>> PROPERTIES_MAPPING = new HashMap<>();

    static {
        // add all to hashmap
        for (BlockProperties property : values()) {
            if (!PROPERTIES_MAPPING.containsKey(property.getGroup())) {
                PROPERTIES_MAPPING.put(property.getGroup(), new HashMap<>());
            }
            PROPERTIES_MAPPING.get(property.getGroup()).put(property.getValue(), property);
        }
    }

    private final String group;
    private final Object value;

    BlockProperties() {
        final String name = name();
        final List<String> split = Arrays.asList(name.split("_"));

        if (name.contains("LEVEL")) {
            // level with int values
            int levelIndex = split.indexOf("LEVEL");
            this.group = split.get(levelIndex - 1).toLowerCase();
        } else if (split.size() == 3) {
            // TYPE_NAME_VALUE
            this.group = split.get(1).toLowerCase();
        } else if (name.endsWith("YES") || name.endsWith("NO")) {
            this.group = split.get(split.size() - 2).toLowerCase();
        } else {
            throw new IllegalArgumentException(String.format("Could not find group automatically: %s", name));
        }
        this.value = getValueByName(name);
    }

    BlockProperties(String group) {
        this.group = group;
        this.value = getValueByName(name());
    }

    BlockProperties(String group, String value) {
        this.group = group;
        this.value = value;
    }

    private static Object getValueByName(String name) {
        final List<String> split = Arrays.asList(name.split("_"));
        if (name.contains("LEVEL")) {
            // level with int values
            return Integer.parseInt(split.get(split.indexOf("LEVEL") + 1));
        } else if (name.endsWith("YES")) {
            return true;
        } else if (name.endsWith("NO")) {
            return false;
        } else if (split.size() == 3) {
            String value = split.get(2).toLowerCase();
            try {
                return Integer.parseInt(value);
            } catch (Exception ignored) {
                return value;
            }
        } else {
            throw new IllegalArgumentException(String.format("Could not find value automatically: %s", name));
        }
    }

    public String getGroup() {
        return this.group;
    }

    public Object getValue() {
        return this.value;
    }
}
