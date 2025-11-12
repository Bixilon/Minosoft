/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.registries.registry

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.wrapper.ObjectField
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import kotlin.reflect.KProperty

abstract class RegistryItem : Identified {
    open val injectable: Boolean get() = true
    private val injects: MutableMap<ObjectField, List<Any>> = if (injectable) HashMap(4) else unsafeNull()

    fun <T : RegistryItem> KProperty<T?>.inject(vararg keys: Any?): T {
        return this.field.inject(*keys)
    }

    fun <T : RegistryItem> ObjectField.inject(vararg keys: Any?): T {
        if (!injectable) throw IllegalStateException("Not injectable")
        if (keys.isEmpty()) return unsafeNull()

        val list: MutableList<Any> = ArrayList(keys.size)
        for (key in keys) {
            if (key == null) continue
            list += key
        }
        if (list.isEmpty()) return unsafeNull()
        injects[this] = list
        return unsafeNull()

    }

    fun inject(registries: Registries) {
        if (!injectable || injects.isEmpty()) return

        for ((field, keys) in injects) {
            var value: Any? = null
            for (key in keys) {
                val registry = registries[field.type as Class<out RegistryItem>] ?: continue
                value = registry[key] ?: continue
                break
            }
            if (value == null) continue

            field[this] = value
        }

        INJECTS_FIELD[this] = null
    }

    open fun postInit(registries: Registries) {}


    override fun toString() = identifier.toString()
    override fun hashCode() = identifier.hashCode()

    override fun equals(other: Any?) = when (other) {
        is RegistryItem -> this.identifier == other.identifier
        is ResourceLocation -> this.identifier == other
        is Identified -> this.identifier == other.identifier
        else -> false
    }

    companion object {
        private val INJECTS_FIELD = RegistryItem::injects.field
    }
}
