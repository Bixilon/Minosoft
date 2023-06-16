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

package de.bixilon.minosoft.data.registries.registries.registry

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import java.lang.reflect.Field
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

abstract class RegistryItem : Identified {
    open val injectable: Boolean get() = true
    private val injects: MutableMap<Field, List<Any>> = if (injectable) hashMapOf() else unsafeNull()

    fun <T : RegistryItem> KProperty<T?>.inject(vararg keys: Any?): T {
        return this.javaField!!.inject(*keys)
    }

    fun <T : RegistryItem> Field.inject(vararg keys: Any?): T {
        if (!injectable) throw IllegalStateException("Not injectable")
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
        if (!injectable) {
            return
        }
        for ((field, keys) in injects) {
            var value: Any? = null
            for (key in keys) {
                value = registries[field.type as Class<out RegistryItem>]?.get(key) ?: continue
                break
            }
            value ?: continue

            field.forceSet(this, value)
        }

        INJECTS_FIELD.set(this, null)
    }

    open fun postInit(registries: Registries) {}


    override fun toString(): String {
        return identifier.toString()
    }

    override fun hashCode(): Int {
        return identifier.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is ResourceLocation) return this.identifier == other
        if (other is Identified) return this.identifier == other.identifier

        return false
    }

    companion object {
        private val INJECTS_FIELD = RegistryItem::injects.javaField!!.apply { isAccessible = true }
    }
}
