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

package de.bixilon.minosoft.gui.rendering.models.block.state.builder.condition

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties

class PropertyCondition(
    val conditions: Map<BlockProperties, Any>,
) : BuilderCondition {

    override fun matches(properties: Map<BlockProperties, Any>): Boolean {
        for ((property, value) in this.conditions) {
            val target = properties[property] ?: return false
            if (value is Set<*>) {
                if (target !in value) return false
            } else {
                if (value != target) return false
            }
        }

        return true
    }

    companion object {

        private fun deserializeOr(property: String, list: List<String>): Pair<BlockProperties, Any> {
            if (list.size == 1) {
                return BlockProperties.parseProperty(property, list.first())
            }

            val values: MutableSet<Any> = mutableSetOf()
            var blockProperty: BlockProperties? = null

            for (entry in list) {
                val (entryProperty, entryValue) = BlockProperties.parseProperty(property, entry)
                if (blockProperty == null) {
                    blockProperty = entryProperty
                }
                values += entryValue
            }
            if (blockProperty == null) {
                throw IllegalStateException("List is empty!")
            }

            return Pair(blockProperty, values)
        }

        fun deserialize(data: JsonObject): PropertyCondition? {
            val properties: MutableMap<BlockProperties, Any> = mutableMapOf()

            for ((key, value) in data) {
                if (key == OrCondition.KEY) continue
                if (key == AndCondition.KEY) continue

                if (value is List<*>) {
                    val (property, values) = deserializeOr(key, value.unsafeCast())
                    properties[property] = values
                    continue
                }
                val split = value.toString().split('|')
                val (property, values) = deserializeOr(key, split)
                properties[property] = values
            }

            if (properties.isEmpty()) return null

            return PropertyCondition(properties)
        }
    }
}
