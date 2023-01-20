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

package de.bixilon.minosoft.gui.rendering.models.unbaked.block

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.SimpleBlockState
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedModel

class SimpleRootModel(
    private val conditions: Map<Map<BlockProperties, Any>, UnbakedModel>,
) : RootModel {

    private fun Map<BlockProperties, Any>.matches(blockState: BlockState): Boolean {
        if (this.isEmpty()) return true
        if (blockState !is SimpleBlockState) return false

        for ((property, value) in this) {
            blockState.properties[property]?.let {
                if (value != it) {
                    return false
                }
            }
        }

        return true
    }

    override fun getModelForState(blockState: BlockState): UnbakedModel {
        for ((condition, model) in conditions) {
            if (condition.matches(blockState)) {
                return model
            }
        }
        TODO("Could not find model for $blockState")
    }

    companion object {
        operator fun invoke(modelLoader: ModelLoader, data: Map<String, Any>): SimpleRootModel {
            val conditions: MutableMap<Map<BlockProperties, Any>, UnbakedModel> = mutableMapOf()


            for ((conditionString, value) in data) {
                val condition: MutableMap<BlockProperties, Any> = mutableMapOf()

                if (conditionString.isNotBlank()) {
                    for (pair in conditionString.split(",")) {
                        val (propertyName, propertyStringValue) = pair.split("=")

                        val (property, propertyValue) = BlockProperties.parseProperty(propertyName, propertyStringValue)

                        condition[property] = propertyValue
                    }
                }

                val model = when (value) {
                    is Map<*, *> -> UnbakedBlockStateModel(modelLoader, value.unsafeCast())
                    is List<*> -> WeightedUnbakedBlockStateModel(modelLoader, value.unsafeCast())
                    else -> TODO("Can not create model: $value")
                }

                conditions[condition] = model
            }

            return SimpleRootModel(conditions)
        }
    }
}
