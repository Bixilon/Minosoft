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

package de.bixilon.minosoft.datafixer

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.datafixer.DataFixerUtil.asResourceLocationMap

object BlockEntityFixer {
    private val RENAMES: Map<ResourceLocation, ResourceLocation> = mapOf(
        "Furnace" to "minecraft:furnace",
        "Furnace" to "minecraft:lit_furnace",
        "Chest" to "minecraft:chest",
        "Chest" to "minecraft:trapped_chest",
        "EnderChest" to "minecraft:ender_chest",
        "RecordPlayer" to "minecraft:jukebox",
        "Trap" to "minecraft:dispenser",
        "Dropper" to "minecraft:dropper",
        "Sign" to "minecraft:sign",
        "MobSpawner" to "minecraft:mob_spawner",
        "Music" to "minecraft:noteblock",
        "Cauldron" to "minecraft:brewing_stand",
        "EnchantTable" to "minecraft:enhanting_table",
        "CommandBlock" to "minecraft:command_block",
        "Beacon" to "minecraft:beacon",
        "Skull" to "minecraft:skull",
        "DLDetector" to "minecraft:daylight_detector",
        "Hopper" to "minecraft:hopper",
        "Banner" to "minecraft:banner",
        "FlowerPot" to "minecraft:flower_pot",
        "CommandBlock" to "minecraft:repeating_command_block",
        "CommandBlock" to "minecraft:chain_command_block",
        "Sign" to "minecraft:standing_sign",
        "Sign" to "minecraft:wall_sign",
        "Piston" to "minecraft:piston_head",
        "DLDetector" to "minecraft:daylight_detector_inverted",
        "Comparator" to "minecraft:unpowered_comparator",
        "Comparator" to "minecraft:powered_comparator",
        "Banner" to "minecraft:wall_banner",
        "Banner" to "minecraft:standing_banner",
        "Structure" to "minecraft:structure_block",
        "Airportal" to "minecraft:end_portal",
        "EndGateway" to "minecraft:end_gateway",
        "Banner" to "minecraft:shield",
        "minecraft:noteblock" to "minecraft:note_block"
    ).asResourceLocationMap()


    fun ResourceLocation.fix(): ResourceLocation {
        return RENAMES.getOrDefault(this, this)
    }
}
