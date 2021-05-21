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

package de.bixilon.minosoft.data.mappings.items.tools

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.types.Block
import de.bixilon.minosoft.data.mappings.versions.Registries

open class AxeItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: JsonObject,
) : MiningToolItem(resourceLocation, registries, data) {
    val strippableBlocks: List<Block>? = data["strippables_blocks"]?.asJsonArray?.let {
        val strippableBlocks: MutableList<Block> = mutableListOf()
        for (id in it) {
            strippableBlocks += registries.blockRegistry[id.asInt]
        }
        strippableBlocks.toList()
    }

}
