/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks.properties

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.properties.primitives.BooleanProperty
import de.bixilon.minosoft.data.registries.blocks.properties.primitives.IntProperty
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.building.dirt.SnowyBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.snow.SnowLayerBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock

@Deprecated("Fallback data")
object BlockProperties {
    private val list: MutableList<BlockProperty<*>> = mutableListOf()


    val POWERED = BooleanProperty("powered").register()
    val TRIGGERED = BooleanProperty("triggered").register()
    val INVERTED = BooleanProperty("inverted").register()
    val LIT = BooleanProperty("lit").register()
    val WATERLOGGED = BooleanProperty("waterlogged").register()
    val STAIR_DIRECTIONAL = EnumProperty("shape", Shapes).register()
    val SLAB_HALF = EnumProperty("half", Halves).register()
    val STAIR_HALF = EnumProperty("half", Halves, Halves.set(Halves.UPPER, Halves.LOWER))
    val SLAB_TYPE = EnumProperty("type", Halves).register()
    val FLUID_LEVEL = FluidBlock.LEVEL.register()
    val MOISTURE_LEVEL = IntProperty("moisture").register()
    val HONEY_LEVEL = IntProperty("honey_level").register()
    val PISTON_EXTENDED = BooleanProperty("extended").register()
    val PISTON_TYPE = EnumProperty("type", PistonTypes).register()
    val PISTON_SHORT = BooleanProperty("short").register()
    val RAILS_SHAPE = EnumProperty("shape", Shapes).register()
    val SNOWY = SnowyBlock.SNOWY.register()
    val STAGE = IntProperty("stage").register()
    val DISTANCE = IntProperty("distance").register()
    val LEAVES_PERSISTENT = BooleanProperty("persistent").register()
    val BED_PART = EnumProperty("part", BedParts).register()
    val BED_OCCUPIED = BooleanProperty("occupied").register()
    val TNT_UNSTABLE = BooleanProperty("unstable").register()
    val DOOR_HINGE = EnumProperty("hinge", Sides).register()
    val DOOR_OPEN = BooleanProperty("open").register()
    val AGE = IntProperty("age").register()
    val INSTRUMENT = EnumProperty("instrument", Instruments).register()
    val NOTE = IntProperty("note").register()
    val REDSTONE_POWER = IntProperty("power").register()

    val MULTIPART_NORTH = EnumProperty("north", MultipartDirections).register()
    val MULTIPART_WEST = EnumProperty("west", MultipartDirections).register()
    val MULTIPART_SOUTH = EnumProperty("south", MultipartDirections).register()
    val MULTIPART_EAST = EnumProperty("east", MultipartDirections).register()
    val MULTIPART_UP = EnumProperty("up", MultipartDirections).register()
    val MULTIPART_DOWN = EnumProperty("down", MultipartDirections).register()

    val SNOW_LAYERS = SnowLayerBlock.LAYERS.register()
    val FENCE_IN_WALL = BooleanProperty("in_wall").register()
    val SCAFFOLDING_BOTTOM = BooleanProperty("bottom").register()
    val TRIPWIRE_DISARMED = BooleanProperty("disarmed").register()
    val TRIPWIRE_IN_AIR = BooleanProperty("in_air").register()
    val TRIPWIRE_ATTACHED = BooleanProperty("attached").register()
    val STRUCTURE_BLOCK_MODE = EnumProperty("mode", StructureBlockModes).register()
    val COMMAND_BLOCK_CONDITIONAL = BooleanProperty("conditional").register()
    val BUBBLE_COLUMN_DRAG = BooleanProperty("drag").register()
    val BELL_ATTACHMENT = EnumProperty("attachment", Attachments).register()
    val LANTERN_HANGING = BooleanProperty("hanging").register()
    val SEA_PICKLE_PICKLES = IntProperty("pickles").register()
    val LECTERN_BOOK = BooleanProperty("has_book").register()

    val BREWING_STAND_BOTTLE_0 = BooleanProperty("has_bottle_0").register()
    val BREWING_STAND_BOTTLE_1 = BooleanProperty("has_bottle_1").register()
    val BREWING_STAND_BOTTLE_2 = BooleanProperty("has_bottle_2").register()

    val CHEST_TYPE = EnumProperty("type", ChestTypes).register()

    val CAKE_BITES = IntProperty("bites").register()
    val BAMBOO_LEAVES = EnumProperty("leaves", BambooLeaves).register()
    val REPEATER_LOCKED = BooleanProperty("locked").register()
    val REPEATER_DELAY = IntProperty("delay").register()
    val PORTAL_FRAME_EYE = BooleanProperty("eye").register()
    val JUKEBOX_HAS_RECORD = BooleanProperty("has_record").register()
    val CAMPFIRE_SIGNAL_FIRE = BooleanProperty("signal_fire").register()
    val TURTLE_EGS_EGGS = IntProperty("eggs").register()
    val TURTLE_EGGS_HATCH = IntProperty("hatch").register()
    val RESPAWN_ANCHOR_CHARGES = IntProperty("charges").register()
    val CANDLES = IntProperty("candles").register()
    val FACE = EnumProperty("face", Attachments).register()
    val HOPPER_ENABLED = BooleanProperty("enabled").register()

    val DRIPSTONE_THICKNESS = EnumProperty("thickness", Thicknesses).register()


    val LEGACY_BLOCK_UPDATE = BooleanProperty("block_update").register()
    val LEGACY_SMOOTH = BooleanProperty("smooth").register()
    val SCULK_SENSOR_PHASE = EnumProperty("sculk_sensor_phase", SensorPhases).register()
    val DRIPSTONE_TILT = EnumProperty("tilt", Tilts).register()
    val CAVE_VINES_BERRIES = BooleanProperty("berries").register()


    val VERTICAL_DIRECTION = EnumProperty("vertical_direction", VerticalDirections).register()


    val LEGACY_CHECK_DECAY = BooleanProperty("check_decay").register()
    val LEGACY_DECAYABLE = BooleanProperty("decayable").register()
    val LEGACY_NODROP = BooleanProperty("nodrop").register()

    val AXIS = EnumProperty("axis", Axes).register()
    val FACING = EnumProperty("facing", Directions).register()
    val FACING_HORIZONTAL = EnumProperty("facing", Directions, Directions.set(Directions.NORTH, Directions.SOUTH, Directions.WEST, Directions.EAST))
    val ROTATION = IntProperty("rotation").register()
    val ORIENTATION = EnumProperty("orientation", Orientations).register()

    val BLOOM = BooleanProperty("bloom").register()

    val SHRIEKING = BooleanProperty("shrieking").register()
    val CAN_SUMMON = BooleanProperty("can_summon").register()

    // ToDo: used in models
    val MAP = BooleanProperty("map").register()

    val BOOKS_STORED = IntProperty("books_stored").register()
    val LAST_INTERACTION_BOOK_SLOT = IntProperty("last_interaction_book_slot").register()

    val BOOK_SLOT_OCCUPIED_0 = BooleanProperty("slot_0_occupied").register()
    val BOOK_SLOT_OCCUPIED_1 = BooleanProperty("slot_1_occupied").register()
    val BOOK_SLOT_OCCUPIED_2 = BooleanProperty("slot_2_occupied").register()
    val BOOK_SLOT_OCCUPIED_3 = BooleanProperty("slot_3_occupied").register()
    val BOOK_SLOT_OCCUPIED_4 = BooleanProperty("slot_4_occupied").register()
    val BOOK_SLOT_OCCUPIED_5 = BooleanProperty("slot_5_occupied").register()


    val DUSTED = IntProperty("dusted").register()
    val FLOWER_AMOUNT = IntProperty("flower_amount").register()

    val CRACKED = BooleanProperty("cracked").register()
    val CRAFTING = BooleanProperty("crafting").register()


    @Deprecated("should not exist")
    fun <T : BlockProperty<*>> T.register(): T {
        list += this
        return this
    }


    val PROPERTIES: Map<String, List<BlockProperty<*>>> = run {
        val map: MutableMap<String, MutableList<BlockProperty<*>>> = mutableMapOf()

        for (value in list) {
            map.getOrPut(value.name) { mutableListOf() } += value
        }

        return@run map
    }

    fun parseProperty(block: Block, group: String, value: Any): Pair<BlockProperty<*>, Any> {
        val property = block.properties[group] ?: return parseProperty(group, value)

        return Pair(property, property.parse(value)!!)
    }

    @Deprecated("not block specific")
    private fun parseProperty(group: String, value: Any): Pair<BlockProperty<*>, Any> {
        val properties = PROPERTIES[group] ?: throw IllegalArgumentException("Can not find group: $group, expected value $value")

        var property: BlockProperty<*>? = null
        var retValue: Any? = null

        for (blockProperty in properties) {
            retValue = try {
                blockProperty.parse(value)
            } catch (exception: Throwable) {
                continue
            }
            property = blockProperty
        }

        if (property == null || retValue == null) {
            throw IllegalArgumentException("Can not parse value $value for group $group")
        }
        return Pair(property, retValue)
    }

    fun BlockState.getFacing(): Directions {
        return this[FACING]
    }

    fun BlockState.isPowered(): Boolean {
        return this[POWERED]
    }

    fun BlockState.isLit(): Boolean {
        return this[LIT]
    }
}
