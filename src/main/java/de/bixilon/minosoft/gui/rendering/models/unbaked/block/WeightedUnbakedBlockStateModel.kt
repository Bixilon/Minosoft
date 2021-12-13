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

package de.bixilon.minosoft.gui.rendering.models.unbaked.block

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.baked.WeightedBakedModel
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedModel

class WeightedUnbakedBlockStateModel(
    val models: List<UnbakedBlockStateModel>,
) : UnbakedModel {

    override fun bake(renderWindow: RenderWindow): BakedModel {
        val baked: MutableMap<BakedBlockModel, Int> = mutableMapOf()

        for (model in models) {
            baked[model.bake(renderWindow)] = model.weight
        }

        return WeightedBakedModel(baked)
    }

    companion object {
        operator fun invoke(modelLoader: ModelLoader, data: List<Map<String, Any>>): WeightedUnbakedBlockStateModel {
            val weightedModels: MutableList<UnbakedBlockStateModel> = mutableListOf()

            for (entry in data) {
                weightedModels += UnbakedBlockStateModel(modelLoader, entry)
            }

            return WeightedUnbakedBlockStateModel(weightedModels)
        }
    }
}
