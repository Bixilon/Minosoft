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

package de.bixilon.minosoft.data.registries.factory.clazz.mapping

import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

open class DefaultClassMappingFactory<T : ClassMappingFactory<*>>(vararg factories: T) {
    private val factoryMap: Map<KClass<*>, T>

    init {
        val ret: MutableMap<KClass<*>, T> = mutableMapOf()


        for (factory in factories) {
            ret[factory.clazz] = factory
            if (factory is MultiClassMappingFactory) {
                for (clazz in factory.aliases) {
                    ret[clazz] = factory
                }
            }
        }

        factoryMap = ret.toMap()
    }

    operator fun get(`class`: KClass<*>?): T? {
        return factoryMap[`class`]
    }

    fun forObject(`object`: Any): T? {
        var clazz: KClass<*> = `object`::class
        while (clazz != Any::class) {
            this[clazz]?.let { return it }
            clazz = clazz.superclasses.firstOrNull() ?: break
        }
        return null
    }
}
