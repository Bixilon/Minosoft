/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.blocks.properties

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.blocks.properties.serializer.BlockPropertiesSerializer
import de.bixilon.minosoft.data.mappings.blocks.properties.serializer.BooleanBlocKPropertiesSerializer
import de.bixilon.minosoft.data.mappings.blocks.properties.serializer.IntBlockPropertiesSerializer
import java.util.*

enum class BlockProperties {
    POWERED(BooleanBlocKPropertiesSerializer),
    TRIGGERED(BooleanBlocKPropertiesSerializer),
    INVERTED(BooleanBlocKPropertiesSerializer),
    LIT(BooleanBlocKPropertiesSerializer),
    WATERLOGGED(BooleanBlocKPropertiesSerializer),
    STAIR_DIRECTIONAL("shape", Shapes),
    STAIR_HALF("half", Halves),
    SLAB_TYPE("type", Halves),
    MOISTURE_LEVEL("moisture", IntBlockPropertiesSerializer),
    FLUID_LEVEL("level", IntBlockPropertiesSerializer),
    HONEY_LEVEL("honey_level", IntBlockPropertiesSerializer),
    PISTON_EXTENDED("extended", BooleanBlocKPropertiesSerializer),
    PISTON_TYPE("type", PistonTypes),
    PISTON_SHORT("short", BooleanBlocKPropertiesSerializer),
    RAILS_SHAPE("shape", Shapes),
    SNOWY(BooleanBlocKPropertiesSerializer),
    STAGE(IntBlockPropertiesSerializer),
    DISTANCE(IntBlockPropertiesSerializer),
    LEAVES_PERSISTENT("persistent", BooleanBlocKPropertiesSerializer),
    BED_PART("part", BedParts),
    BED_OCCUPIED("occupied", BooleanBlocKPropertiesSerializer),
    TNT_UNSTABLE("unstable", BooleanBlocKPropertiesSerializer),
    DOOR_HINGE("hinge", Sides),
    DOOR_OPEN("open", BooleanBlocKPropertiesSerializer),
    AGE(IntBlockPropertiesSerializer),
    INSTRUMENT(Instruments),
    NOTE(IntBlockPropertiesSerializer),
    REDSTONE_POWER("power", IntBlockPropertiesSerializer),

    MULTIPART_NORTH("north", MultipartDirectionParser),
    MULTIPART_WEST("west", MultipartDirectionParser),
    MULTIPART_SOUTH("south", MultipartDirectionParser),
    MULTIPART_EAST("east", MultipartDirectionParser),
    MULTIPART_UP("up", MultipartDirectionParser),
    MULTIPART_DOWN("down", MultipartDirectionParser),

    SNOW_LAYERS("layers", IntBlockPropertiesSerializer),
    FENCE_IN_WALL("in_wall", BooleanBlocKPropertiesSerializer),
    SCAFFOLDING_BOTTOM("bottom", BooleanBlocKPropertiesSerializer),
    TRIPWIRE_DISARMED("disarmed", BooleanBlocKPropertiesSerializer),
    TRIPWIRE_IN_AIR("in_air", BooleanBlocKPropertiesSerializer),
    TRIPWIRE_ATTACHED("attached", BooleanBlocKPropertiesSerializer),
    STRUCTURE_BLOCK_MODE("mode", StructureBlockModes),
    COMMAND_BLOCK_CONDITIONAL("conditional", BooleanBlocKPropertiesSerializer),
    BUBBLE_COLUMN_DRAG("drag", BooleanBlocKPropertiesSerializer),
    BELL_ATTACHMENT("attachment", Attachments),
    LANTERN_HANGING("hanging", BooleanBlocKPropertiesSerializer),
    SEA_PICKLE_PICKLES("pickles", IntBlockPropertiesSerializer),
    LECTERN_BOOK("has_book", BooleanBlocKPropertiesSerializer),

    BREWING_STAND_BOTTLE_0("has_bottle_0", BooleanBlocKPropertiesSerializer),
    BREWING_STAND_BOTTLE_1("has_bottle_1", BooleanBlocKPropertiesSerializer),
    BREWING_STAND_BOTTLE_2("has_bottle_2", BooleanBlocKPropertiesSerializer),

    CHEST_TYPE("type", ChestTypes),

    CAKE_BITES("bites", IntBlockPropertiesSerializer),
    BAMBOO_LEAVES("leaves", BambooLeaves),
    REPEATER_LOCKED("locked", BooleanBlocKPropertiesSerializer),
    REPEATER_DELAY("delay", IntBlockPropertiesSerializer),
    PORTA_FRAME_EYE("eye", BooleanBlocKPropertiesSerializer),
    JUKEBOX_HAS_RECORD("has_record", BooleanBlocKPropertiesSerializer),
    CAMPFIRE_SIGNAL_FIRE("signal_fire", BooleanBlocKPropertiesSerializer),
    TURTLE_EGS_EGGS("eggs", IntBlockPropertiesSerializer),
    TURTLE_EGGS_HATCH("hatch", IntBlockPropertiesSerializer),
    RESPAWN_ANCHOR_CHARGES("charges", IntBlockPropertiesSerializer),
    CANDLES(IntBlockPropertiesSerializer),
    FACE("face", Attachments),
    HOPPER_ENABLED("enabled", BooleanBlocKPropertiesSerializer),

    DRIPSTONE_THICKNESS("thickness", Thicknesses),


    LEGACY_BLOCK_UPDATE("block_update", BooleanBlocKPropertiesSerializer),
    LEGACY_SMOOTH("smooth", BooleanBlocKPropertiesSerializer),
    SCULK_SENSOR_PHASE("sculk_sensor_phase", SensorPhases),
    DRIPSTONE_TILT("tilt", Tilts),
    CAVE_VINES_BERRIES("berries", BooleanBlocKPropertiesSerializer),


    VERTICAL_DIRECTION("vertical_direction", VerticalDirections),


    LEGACY_CHECK_DECAY("check_decay", BooleanBlocKPropertiesSerializer),
    LEGAVY_DECAYABLE("decayable", BooleanBlocKPropertiesSerializer),
    LEGAVY_NODROP("nodrop", BooleanBlocKPropertiesSerializer),

    AXIS("axis", Axes),
    FACING("facing", Directions),
    ROTATION("rotation", IntBlockPropertiesSerializer),
    ORIENTATION("orientation", Orientations),

    ;

    val group: String
    val serializer: BlockPropertiesSerializer

    constructor(group: String, serializer: BlockPropertiesSerializer) {
        this.group = group
        this.serializer = serializer
    }

    constructor(serializer: BlockPropertiesSerializer) {
        this.group = name.lowercase(Locale.getDefault())
        this.serializer = serializer
    }

    companion object {
        private val PROPERTIES: Map<String, List<BlockProperties>> = run {
            val map: MutableMap<String, MutableList<BlockProperties>> = mutableMapOf()

            for (value in values()) {
                val list = map.getOrPut(value.group, { mutableListOf() })
                list.add(value)
            }

            return@run map.toMap()
        }

        fun parseProperty(group: String, value: Any): Pair<BlockProperties, Any> {
            PROPERTIES[group]?.let {
                var retProperty: BlockProperties? = null
                var retValue: Any? = null

                for (blockProperty in it) {
                    retValue = try {
                        blockProperty.serializer.deserialize(value)
                    } catch (exception: Throwable) {
                        continue
                    }
                    retProperty = blockProperty
                }

                if (retProperty == null || retValue == null) {
                    throw IllegalArgumentException("Can not parse value $value for group $group")
                }
                return Pair(retProperty, retValue)
            } ?: throw IllegalArgumentException("Can not find group: $group, expected value $value")
        }
    }
}
