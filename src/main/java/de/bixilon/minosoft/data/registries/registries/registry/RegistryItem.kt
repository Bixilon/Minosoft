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
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

abstract class RegistryItem : Identified {
    open val injectable: Boolean get() = true
    private val injects: MutableMap<KProperty<RegistryItem?>, List<Any>> = if (injectable) mutableMapOf() else unsafeNull()

    fun <T : RegistryItem> KProperty<T?>.inject(vararg keys: Any?): T {
        if (!injectable) {
            throw IllegalStateException("Not injectable")
        }
        val keyList: MutableList<Any> = mutableListOf()
        for (key in keys) {
            key ?: continue
            keyList += key
        }
        if (keyList.isEmpty()) {
            return unsafeNull()
        }
        injects[this] = keyList
        return unsafeNull()
    }

    fun inject(registries: Registries) {
        if (!injectable) {
            return
        }
        for ((field, keys) in injects) {
            val javaField = field.javaField ?: continue
            var value: Any? = null
            for (key in keys) {
                value = registries[javaField.type as Class<out RegistryItem>]?.get(key) ?: continue
                break
            }
            value ?: continue

            javaField.forceSet(this, value)
        }

        this::injects.javaField?.forceSet(this, null)
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
}
