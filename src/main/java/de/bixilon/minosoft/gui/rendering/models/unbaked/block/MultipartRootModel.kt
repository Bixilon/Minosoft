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

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.gui.rendering.models.unbaked.GenericUnbakedModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedModel
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast

class MultipartRootModel(
    private val conditions: MutableMap<MutableSet<Set<Map<BlockProperties, Any>>>, Set<UnbakedBlockStateModel>>,
) : RootModel {

    private fun Set<Map<BlockProperties, Any>>.matches(blockState: BlockState): Boolean {
        var matches = false

        for (propertyMap in this) {
            var singleMatches = false
            for ((property, value) in propertyMap) {
                if (blockState.properties[property] == value) {
                    singleMatches = true
                    break
                }
            }
            if (singleMatches) {
                matches = true
                break
            }
        }

        return matches
    }

    private fun MutableSet<Set<Map<BlockProperties, Any>>>.matchesAny(blockState: BlockState): Boolean {
        var matches = true
        for (or in this) {
            if (!or.matches(blockState)) {
                matches = false
                break
            }
        }
        return matches
    }

    override fun getModelForState(blockState: BlockState): UnbakedModel {
        val models: MutableSet<UnbakedBlockStateModel> = mutableSetOf()

        for ((condition, apply) in conditions) {
            if (condition.matchesAny(blockState)) {
                models += apply
            }
        }

        return UnbakedMultipartModel(models)
    }

    companion object {

        private fun getCondition(data: MutableMap<String, Any>): MutableSet<Map<BlockProperties, Any>> {
            val singleCondition: MutableSet<Map<BlockProperties, Any>> = mutableSetOf()
            for ((propertyName, value) in data) {
                val properties: MutableMap<BlockProperties, Any> = mutableMapOf()

                for (propertyValue in value.toString().split("|")) {
                    properties += BlockProperties.parseProperty(propertyName, propertyValue)
                }
                singleCondition += properties
            }
            return singleCondition
        }

        operator fun invoke(models: Map<ResourceLocation, GenericUnbakedModel>, data: List<Any>): MultipartRootModel {
            val conditions: MutableMap<MutableSet<Set<Map<BlockProperties, Any>>>, Set<UnbakedBlockStateModel>> = mutableMapOf()


            for (modelData in data) {
                check(modelData is Map<*, *>)
                val condition: MutableSet<Set<Map<BlockProperties, Any>>> = mutableSetOf()
                val applyData = modelData["apply"]!!
                val apply: MutableSet<UnbakedBlockStateModel> = mutableSetOf()
                if (applyData is Map<*, *>) {
                    apply += UnbakedBlockStateModel(models, applyData.unsafeCast())
                } else if (applyData is List<*>) {
                    for (applyModelData in applyData) {
                        apply += UnbakedBlockStateModel(models, applyModelData.unsafeCast())
                    }
                }

                modelData["when"]?.compoundCast()?.let {
                    val or = it["OR"]
                    if (or is List<*>) {
                        for (orData in or) {
                            condition += getCondition(orData.unsafeCast())
                        }
                        return@let
                    }
                    condition += getCondition(it)
                }



                conditions[condition] = apply
            }

            return MultipartRootModel(conditions)
        }
    }
}
