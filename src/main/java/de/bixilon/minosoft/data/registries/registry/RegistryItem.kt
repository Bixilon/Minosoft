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

package de.bixilon.minosoft.data.registries.registry

import de.bixilon.minosoft.data.registries.ResourceLocationAble
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.items.BlockItem
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.util.KUtil.setValue
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

abstract class RegistryItem : ResourceLocationAble {
    private val injects: MutableMap<KProperty<RegistryItem?>, List<Any>> = mutableMapOf()

    fun KProperty<RegistryItem?>.inject(vararg keys: Any?) {
        val keyList: MutableList<Any> = mutableListOf()
        for (key in keys) {
            key ?: continue
            keyList += key
        }
        if (keyList.isEmpty()) {
            return
        }
        injects[this] = keyList
    }

    fun inject(registries: Registries) {
        for ((field, keys) in injects) {
            val javaField = field.javaField ?: continue
            var value: Any? = null
            for (key in keys) {
                val currentValue = registries[javaField.type as Class<out RegistryItem>]?.get(key) ?: continue
                value = currentValue
                break
            }
            value ?: continue

            javaField.setValue(this, value)
        }
    }

    open fun postInit(registries: Registries) { }
}
