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

import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.data.entities.StatusEffectInstance
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.effects.StatusEffectType
import de.bixilon.minosoft.data.registries.effects.attributes.EntityAttributeModifier

class StatusEffectProperty : Tickable {
    private val effects: LockMap<StatusEffectType, StatusEffectInstance> = lockMapOf()


    override fun tick() {
        effects.lock.lock()
        val remove: MutableSet<StatusEffectType> = mutableSetOf()
        for ((effect, instance) in effects.unsafe) {
            instance.tick()
            if (instance.expired) {
                remove += effect
            }
        }
        this.effects.unsafe -= remove
        effects.lock.unlock()
    }

    fun add(instance: StatusEffectInstance) {
        this.effects[instance.type] = instance
    }

    operator fun plusAssign(instance: StatusEffectInstance) = add(instance)

    fun remove(effect: StatusEffectType): StatusEffectInstance? {
        return this.effects.remove(effect)
    }

    operator fun minusAssign(effect: StatusEffectType) {
        remove(effect)
    }

    operator fun get(effect: StatusEffectType?): StatusEffectInstance? {
        return effects[effect]
    }

    operator fun contains(effect: StatusEffectType?): Boolean {
        return effect in this.effects
    }

    fun processAttribute(name: ResourceLocation): MutableMap<EntityAttributeModifier, Int> {
        val attributes: MutableMap<EntityAttributeModifier, Int> = mutableMapOf()
        effects.lock.acquire()
        for ((type, instance) in this.effects) {
            val attribute = type.attributes[name] ?: continue
            attributes[attribute] = instance.amplifier
        }
        effects.lock.release()
        return attributes
    }
}
