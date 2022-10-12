/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.datafixer.rls

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.datafixer.DataFixerUtil.asResourceLocationMap

object BlockEntityFixer : ResourceLocationFixer {
    private val RENAMES: Map<ResourceLocation, ResourceLocation> = mapOf(
        "Furnace" to "minecraft:furnace",
        "Chest" to "minecraft:chest",
        "EnderChest" to "minecraft:ender_chest",
        "RecordPlayer" to "minecraft:jukebox",
        "Trap" to "minecraft:dispenser",
        "Dropper" to "minecraft:dropper",
        "Sign" to "minecraft:sign",
        "wall_sign" to "minecraft:sign",
        "MobSpawner" to "minecraft:mob_spawner",
        "Music" to "minecraft:note_block",
        "Cauldron" to "minecraft:brewing_stand",
        "EnchantTable" to "minecraft:enchanting_table",
        "CommandBlock" to "minecraft:command_block",
        "Beacon" to "minecraft:beacon",
        "Skull" to "minecraft:skull",
        "DLDetector" to "minecraft:daylight_detector",
        "Hopper" to "minecraft:hopper",
        "Banner" to "minecraft:banner",
        "FlowerPot" to "minecraft:flower_pot",
        "Sign" to "minecraft:sign",
        "Piston" to "minecraft:piston",
        "Comparator" to "minecraft:comparator",
        "minecraft:unpowered_comparator" to "minecraft:comparator",
        "minecraft:powered_comparator" to "minecraft:comparator",
        "Structure" to "minecraft:structure_block",
        "Airportal" to "minecraft:end_portal",
        "EndGateway" to "minecraft:end_gateway",
        "minecraft:noteblock" to "minecraft:note_block",
        "Bed" to "minecraft:bed",
    ).asResourceLocationMap()


    override fun _fix(resourceLocation: ResourceLocation): ResourceLocation {
        return RENAMES.getOrDefault(resourceLocation, resourceLocation)
    }
}
