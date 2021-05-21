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
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.versions.Registries

open class HoeItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: JsonObject,
) : MiningToolItem(resourceLocation, registries, data) {
    val tillableBlocKStates: List<BlockState>? = data["tillables_block_states"]?.asJsonArray?.let {
        val diggableBlocks: MutableList<BlockState> = mutableListOf()
        for (id in it) {
            diggableBlocks += registries.getBlockState(id.asInt)!!
        }
        diggableBlocks.toList()
    }

}
