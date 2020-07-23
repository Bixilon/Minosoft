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

package de.bixilon.minosoft.game.datatypes.blocks;

public enum BlockProperties {
    NONE,

    // farmland
    MOISTURE_0,
    MOISTURE_1,
    MOISTURE_2,
    MOISTURE_3,
    MOISTURE_4,
    MOISTURE_5,
    MOISTURE_6,
    MOISTURE_7,

    // furnace, ...
    LIT,
    UN_LIT,

    // sign, fence
    WATERLOGGED,
    NOT_WATERLOGGED,

    // half (flowers)
    HALF_UPPER,
    HALF_LOWER,

    // slabs
    SLAB_TOP,
    SLAB_BOTTOM,
    SLAB_DOUBLE,

    // fluids
    LEVEL_0,
    LEVEL_1,
    LEVEL_2,
    LEVEL_3,
    LEVEL_4,
    LEVEL_5,
    LEVEL_6,
    LEVEL_7,
    LEVEL_8,
    LEVEL_9,
    LEVEL_10,
    LEVEL_11,
    LEVEL_12,
    LEVEL_13,
    LEVEL_14,
    LEVEL_15,

    // bee hive
    HONEY_LEVEL_0,
    HONEY_LEVEL_1,
    HONEY_LEVEL_2,
    HONEY_LEVEL_3,
    HONEY_LEVEL_4,
    HONEY_LEVEL_5,

    // pistons
    TYPE_NORMAL,
    TYPE_STICKY,
    EXTENDED,
    NOT_EXTENDED,
    SHORT,
    LONG,

    // rails
    POWERED,
    NOT_POWERED,
    STRAIGHT,
    INNER_LEFT,
    INNER_RIGHT,
    OUTER_LEFT,
    OUTER_RIGHT,
    NORTH_SOUTH,
    SOUTH_EAST,
    SOUTH_WEST,
    NORTH_WEST,
    NORTH_EAST,
    EAST_WEST,
    ASCENDING_EAST,
    ASCENDING_WEST,
    ASCENDING_NORTH,
    ASCENDING_SOUTH,

    SNOWY,
    NOT_SNOWY,

    STAGE_0,
    STAGE_1,

    // dispenser
    TRIGGERED,
    NOT_TRIGGERED,

    // leaves
    DISTANCE_0,
    DISTANCE_1,
    DISTANCE_2,
    DISTANCE_3,
    DISTANCE_4,
    DISTANCE_5,
    DISTANCE_6,
    DISTANCE_7,
    PERSISTENT,
    NOT_PERSISTENT,

    // bed
    HEAD,
    FOOT,
    OCCUPIED,
    NOT_OCCUPIED,

    // tnt
    UNSTABLE,
    STABLE,

    // door
    HINGE_LEFT,
    HINGE_RIGHT,
    OPEN,
    CLOSED,

    // fire
    NORTH,
    NOT_NORTH,
    SOUTH,
    NOT_SOUTH,
    EAST,
    NOT_EAST,
    WEST,
    NOT_WEST,
    UP,
    NOT_UP,
    DOWN,
    NOT_DOWN,
    AGE_0,
    AGE_1,
    AGE_2,
    AGE_3,
    AGE_4,
    AGE_5,
    AGE_6,
    AGE_7,
    AGE_8,
    AGE_9,
    AGE_10,
    AGE_11,
    AGE_12,
    AGE_13,
    AGE_14,
    AGE_15,
    AGE_16,
    AGE_17,
    AGE_18,
    AGE_19,
    AGE_20,
    AGE_21,
    AGE_22,
    AGE_23,
    AGE_24,
    AGE_25,

    // noteblock
    HARP,
    BASEDRUM,
    SNARE,
    HAT,
    BASS,
    FLUTE,
    BELL,
    GUITAR,
    CHIME,
    XYLOPHONE,
    IRON_XYLOPHONE,
    COW_BELL,
    DIDGERIDOO,
    BIT,
    BANJO,
    PLING,

    NOTE_0,
    NOTE_1,
    NOTE_2,
    NOTE_3,
    NOTE_4,
    NOTE_5,
    NOTE_6,
    NOTE_7,
    NOTE_8,
    NOTE_9,
    NOTE_10,
    NOTE_11,
    NOTE_12,
    NOTE_13,
    NOTE_14,
    NOTE_15,
    NOTE_16,
    NOTE_17,
    NOTE_18,
    NOTE_19,
    NOTE_20,
    NOTE_21,
    NOTE_22,
    NOTE_23,
    NOTE_24,


    // redstone
    POWER_0,
    POWER_1,
    POWER_2,
    POWER_3,
    POWER_4,
    POWER_5,
    POWER_6,
    POWER_7,
    POWER_8,
    POWER_9,
    POWER_10,
    POWER_11,
    POWER_12,
    POWER_13,
    POWER_14,
    POWER_15,
    NORTH_UP,
    SOUTH_UP,
    EAST_UP,
    WEST_UP,
    NORTH_SIDE,
    SOUTH_SIDE,
    EAST_SIDE,
    WEST_SIDE,

    LAYERS_1,
    LAYERS_2,
    LAYERS_3,
    LAYERS_4,
    LAYERS_5,
    LAYERS_6,
    LAYERS_7,
    LAYERS_8,

    IN_WALL,
    NOT_IN_WALL,

    // scaffolding
    BOTTOM,
    NOT_BOTTOM,

    // log, portal
    AXIS_X,
    AXIS_Y,
    AXIS_Z,

    // trapwire
    DISARMED,
    ARMED,
    ATTACHED,
    NOT_ATTACHED,
    IN_AIR,
    ON_GROUND,

    // daylight, etc
    INVERTED,
    NOT_INVERTED,

    // button
    FLOOR,
    WALL,
    CEILING,

    // structure block, comparator
    SAVE,
    LOAD,
    CORNER,
    DATA,
    COMPARE,
    SUBTRACT,

    // command block
    CONDITIONAL,
    UNCONDITIONAL,

    // double column
    DRAG,
    NOT_DRAG,

    // bell
    SINGLE_WALL,
    DOUBLE_WALL,

    // lantern
    HANGING,
    NOT_HANGING,

    // sea pickle
    PICKLES_1,
    PICKLES_2,
    PICKLES_3,
    PICKLES_4,

    // lectern
    HAS_BOOK,
    NO_BOOK,

    // brewing stand
    HAS_BOTTLE_0,
    NO_BOTTLE_0,
    HAS_BOTTLE_1,
    NO_BOTTLE_1,
    HAS_BOTTLE_2,
    NO_BOTTLE_2,

    // chest
    TYPE_SINGLE,
    TYPE_LEFT,
    TYPE_RIGHT,

    // cake
    BITES_0,
    BITES_1,
    BITES_2,
    BITES_3,
    BITES_4,
    BITES_5,
    BITES_6,


    // bamboo
    SMALL,
    LARGE,

    // repeater
    LOCKED,
    UNLOCKED,
    DELAY_1,
    DELAY_2,
    DELAY_3,
    DELAY_4,

    // end portal frame
    EYE,
    NO_EYE,

    // jukebox
    HAS_RECORD,
    HAS_NO_RECORD,

    // campfire
    SIGNAL_FIRE,
    NOT_SIGNAL_FIRE,

    // turtle eggs
    EGGS_1,
    EGGS_2,
    EGGS_3,
    EGGS_4,
    // turtle eggs
    HATCH_0,
    HATCH_1,
    HATCH_2,

    ENABLED,
    DISABLED
}
