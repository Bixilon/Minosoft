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

class AndCondition(
    val conditions: Set<BuilderCondition>,
) : BuilderCondition {

    override fun matches(properties: Map<BlockProperties, Any>): Boolean {
        for (condition in conditions) {
            if (!condition.matches(properties)) return false
        }
        return true
    }

    companion object {
        const val KEY = "AND"

        fun deserialize(data: List<JsonObject>): BuilderCondition? {
            val conditions: MutableSet<PropertyCondition> = mutableSetOf()

            for (entry in data) {
                conditions += PropertyCondition.deserialize(entry) ?: continue
            }
            if (conditions.isEmpty()) return null

            return AndCondition(conditions) // TODO: They can be compacted into one Property condition, could speed up memory usage and performance a bit
        }

        fun deserialize(data: JsonObject): BuilderCondition? {
            if (data.isEmpty()) return null

            val conditions: MutableSet<BuilderCondition> = mutableSetOf()

            PropertyCondition.deserialize(data)?.let { conditions += it }
            data[KEY]?.let { deserialize(it.unsafeCast<List<JsonObject>>()) }?.let { conditions += it }
            data[OrCondition.KEY]?.let { OrCondition.deserialize(it.unsafeCast()) }?.let { conditions += it }

            if (conditions.isEmpty()) return null

            return AndCondition(conditions)
        }
    }
}
