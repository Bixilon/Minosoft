/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.entities.properties

import de.bixilon.kutil.collections.CollectionUtil
import de.bixilon.minosoft.data.entities.StatusEffectInstance
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.effects.StatusEffect
import de.bixilon.minosoft.data.registries.effects.attributes.EntityAttribute
import de.bixilon.minosoft.data.registries.effects.attributes.EntityAttributeModifier
import de.bixilon.minosoft.data.registries.effects.attributes.StatusEffectOperations

class ModifierEntityProperty(private val entity: Entity) {
    val activeStatusEffects: MutableMap<StatusEffect, StatusEffectInstance> = CollectionUtil.synchronizedMapOf()
    val attributes: MutableMap<ResourceLocation, EntityAttribute> = CollectionUtil.synchronizedMapOf()

    fun addEffect(effect: StatusEffectInstance) {
        // effect already applied, maybe the duration or the amplifier changed?
        removeEffect(effect.statusEffect)
        activeStatusEffects[effect.statusEffect] = effect
    }

    fun removeEffect(effect: StatusEffect) {
        activeStatusEffects.remove(effect)
    }

    fun getAttributeValue(name: ResourceLocation, baseValue: Double? = null): Double {
        // ToDo: Check order and verify value
        val attribute = attributes[name]
        val realBaseValue = baseValue ?: attribute?.baseValue ?: entity.type.attributes[name] ?: 1.0
        var ret = realBaseValue

        fun addToValue(modifier: EntityAttributeModifier, amplifier: Int) {
            val instanceValue = modifier.amount * amplifier
            when (modifier.operation) {
                StatusEffectOperations.MULTIPLY_TOTAL -> ret *= 1.0 + instanceValue
                StatusEffectOperations.ADDITION -> ret += instanceValue
                StatusEffectOperations.MULTIPLY_BASE -> ret += realBaseValue * (instanceValue + 1.0)
            }
        }

        attribute?.let {
            for (instance in it.modifiers.values) {
                addToValue(instance, 1)
            }
        }

        for (statusEffect in activeStatusEffects.values) {
            for ((instanceResourceLocation, instance) in statusEffect.statusEffect.attributes) {
                if (instanceResourceLocation != name) {
                    continue
                }
                addToValue(instance, statusEffect.amplifier)
            }
        }

        return ret
    }
}
