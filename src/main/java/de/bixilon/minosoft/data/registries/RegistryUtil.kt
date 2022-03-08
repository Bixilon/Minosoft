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

package de.bixilon.minosoft.data.registries

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.unsafe.UnsafeUtil
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object RegistryUtil {
    private val parameterizedClass = Class.forName("sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl")
    private val rawTypeField = parameterizedClass.getDeclaredField("rawType")
    private val offset = UnsafeUtil.UNSAFE.objectFieldOffset(rawTypeField)

    fun getClassOfFactory(type: Type): Class<*> {
        val actualType = type.unsafeCast<ParameterizedType>().actualTypeArguments.first()
        return if (actualType is Class<*>) {
            actualType
        } else if (actualType::class.java == parameterizedClass) {
            UnsafeUtil.UNSAFE.getObject(actualType, offset).unsafeCast<Class<*>>()
        } else {
            TODO()
        }
    }
}
