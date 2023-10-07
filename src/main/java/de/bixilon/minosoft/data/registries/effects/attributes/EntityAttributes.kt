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

package de.bixilon.minosoft.data.registries.effects.attributes

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.minosoft.data.registries.effects.attributes.container.AttributeContainer
import de.bixilon.minosoft.data.registries.effects.attributes.container.AttributeContainerUpdate
import de.bixilon.minosoft.data.registries.effects.attributes.container.AttributeModifier
import de.bixilon.minosoft.data.registries.effects.attributes.integrated.IntegratedAttribute
import java.util.*

class EntityAttributes(
    private val fallback: Map<AttributeType, Double> = emptyMap(),
) {
    private val attributes: LockMap<AttributeType, AttributeContainer> = lockMapOf()


    fun getOrCreate(type: AttributeType, base: Double? = null): AttributeContainer {
        return attributes.synchronizedGetOrPut(type) { AttributeContainer(base ?: fallback[type] ?: type.fallback) }
    }

    fun update(attributes: Map<AttributeType, AttributeContainerUpdate>) {
        for ((type, update) in attributes) {
            getOrCreate(type, update.base).update(update)
        }
    }

    fun add(type: AttributeType, modifier: AttributeModifier) {
        getOrCreate(type).add(modifier)
    }

    operator fun plusAssign(modifier: IntegratedAttribute) = add(modifier.attribute, modifier.modifier)

    fun remove(type: AttributeType, modifier: UUID) {
        attributes[type]?.remove(modifier)
    }

    fun remove(type: AttributeType, modifier: AttributeModifier) = remove(type, modifier.uuid)
    operator fun minusAssign(modifier: IntegratedAttribute) = remove(modifier.attribute, modifier.modifier.uuid)


    private fun process(attributes: AttributeContainer?): Map<AttributeOperations, Set<AttributeModifier>> {
        // ToDo: Deduplicate uuids?
        val result: MutableMap<AttributeOperations, MutableSet<AttributeModifier>> = mutableMapOf()
        if (attributes != null) {
            for (modifier in attributes) {
                result.getOrPut(modifier.operation) { mutableSetOf() } += modifier
            }
        }
        return result
    }

    operator fun get(type: AttributeType, fallback: Double = this.fallback[type] ?: type.fallback): Double {
        val attributes = this.attributes[type]
        val base = attributes?.base ?: fallback
        val modifiers = process(attributes)

        var added = base
        for (modifier in modifiers[AttributeOperations.ADD] ?: emptySet()) {
            added += modifier.amount
        }
        var value = added
        for (modifier in modifiers[AttributeOperations.MULTIPLY_BASE] ?: emptySet()) {
            value += base * modifier.amount
        }
        for (modifier in modifiers[AttributeOperations.MULTIPLY_TOTAL] ?: emptySet()) {
            value *= 1.0 + modifier.amount
        }

        return value.clamp(type.min, type.max)
    }
}
