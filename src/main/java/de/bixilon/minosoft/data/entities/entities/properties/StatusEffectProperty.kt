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
import de.bixilon.minosoft.data.registries.effects.StatusEffectType

class StatusEffectProperty : Tickable {
    private val effects: LockMap<StatusEffectType, StatusEffectInstance> = lockMapOf()


    override fun tick() {
        effects.lock.lock()
        val iterator = effects.unsafe.iterator()
        for ((effect, instance) in iterator) {
            instance.tick()
            if (instance.expired) {
                iterator.remove()
            }
        }
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
}
