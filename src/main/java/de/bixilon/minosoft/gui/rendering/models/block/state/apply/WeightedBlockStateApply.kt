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

package de.bixilon.minosoft.gui.rendering.models.block.state.apply

import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.gui.rendering.models.block.state.render.WeightedBlockRender
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager

data class WeightedBlockStateApply(
    val models: List<WeightedApply>
) : BlockStateApply {

    override fun bake(textures: TextureManager): WeightedBlockRender? {
        val baked: Array<WeightedBlockRender.WeightedEntry?> = arrayOfNulls(models.size)
        var totalWeight = 0

        for ((index, entry) in models.withIndex()) {
            val model = entry.apply.bake(textures) ?: continue
            baked[index] = WeightedBlockRender.WeightedEntry(entry.weight, model)
            totalWeight += entry.weight
        }

        if (totalWeight == 0) return null

        return WeightedBlockRender(baked.cast(), totalWeight)
    }

    data class WeightedApply(
        val weight: Int,
        val apply: SingleBlockStateApply,
    )

    companion object {

        fun deserialize(loader: BlockLoader, data: List<JsonObject>): WeightedBlockStateApply? {
            if (data.isEmpty()) return null
            val models: MutableList<WeightedApply> = ArrayList(data.size)

            for (entry in data) {
                var weight = entry["weight"]?.toInt() ?: 1
                if (weight < 0) weight = 1
                val apply = SingleBlockStateApply.deserialize(loader, entry) ?: continue
                models += WeightedApply(weight, apply)
            }
            if(models.isEmpty()) return null

            return WeightedBlockStateApply(models)
        }
    }
}
