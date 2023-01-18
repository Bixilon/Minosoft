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
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.unbaked.AbstractUnbakedBlockModel

class MultipartRootModel(
    private val conditions: MutableMap<MutableSet<Map<BlockProperties, Set<Any>>>, MutableSet<AbstractUnbakedBlockModel>>,
) : RootModel {

    private fun Map<BlockProperties, Set<Any>>.matches(blockState: BlockState): Boolean {
        var matches = true

        for ((property, values) in this) {
            var singleMatches = false
            for (value in values) {
                if (blockState.properties[property] == value) {
                    singleMatches = true
                    break
                }
            }
            if (!singleMatches) {
                matches = false
                break
            }
        }

        return matches
    }

    private fun Set<Map<BlockProperties, Set<Any>>>.matchesAny(blockState: BlockState): Boolean {
        var matches = true
        for (or in this) {
            if (!or.matches(blockState)) {
                matches = false
                continue
            }
            matches = true
            break
        }
        return matches
    }

    override fun getModelForState(blockState: BlockState): AbstractUnbakedBlockModel {
        val models: MutableSet<AbstractUnbakedBlockModel> = mutableSetOf()

        for ((condition, apply) in conditions) {
            if (condition.matchesAny(blockState)) {
                models += apply
            }
        }

        return UnbakedMultipartModel(models)
    }

    companion object {

        private fun getCondition(data: JsonObject): MutableMap<BlockProperties, Set<Any>> {
            val condition: MutableMap<BlockProperties, Set<Any>> = mutableMapOf()
            for ((propertyName, value) in data) {
                var property: BlockProperties? = null
                val values: MutableSet<Any> = mutableSetOf()

                for (propertyValue in value.toString().split("|")) {
                    val (parsedProperty, parsedValue) = BlockProperties.parseProperty(propertyName, propertyValue)
                    if (property == null) {
                        property = parsedProperty
                    }
                    values += parsedValue
                }
                condition[property!!] = values
            }
            return condition
        }

        operator fun invoke(modelLoader: ModelLoader, data: List<Any>): MultipartRootModel {
            val conditions: MutableMap<MutableSet<Map<BlockProperties, Set<Any>>>, MutableSet<AbstractUnbakedBlockModel>> = mutableMapOf()


            for (modelData in data) {
                check(modelData is Map<*, *>)
                val condition: MutableSet<Map<BlockProperties, Set<Any>>> = mutableSetOf()
                val applyData = modelData["apply"]!!
                val apply: MutableSet<AbstractUnbakedBlockModel> = mutableSetOf()
                if (applyData is Map<*, *>) {
                    apply += UnbakedBlockStateModel(modelLoader, applyData.unsafeCast())
                } else if (applyData is List<*>) {
                    apply += WeightedUnbakedBlockStateModel(modelLoader, applyData.unsafeCast())
                }

                modelData["when"]?.toJsonObject()?.let {
                    val or = it["OR"]
                    if (or is List<*>) {
                        for (orData in or) {
                            condition += getCondition(orData.unsafeCast())
                        }
                        return@let
                    }
                    val and = it["AND"]
                    if (and is List<*>) {
                        val andCondition: MutableMap<BlockProperties, Set<Any>> = mutableMapOf()
                        for (andData in and) {
                            andCondition += getCondition(andData.unsafeCast())
                        }
                        condition += andCondition
                        return@let
                    }
                    condition += getCondition(it)
                }



                conditions.getOrPut(condition) { mutableSetOf() } += apply
            }

            return MultipartRootModel(conditions)
        }
    }
}
