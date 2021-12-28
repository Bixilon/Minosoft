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

package de.bixilon.minosoft.util

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast

@Deprecated("Will be part of KUtil in next version")
object CastUtil {

    fun Any?.asAnyCollection(): Collection<Any> {
        return this.unsafeCast()
    }

    fun Any?.toAnyCollection(): Collection<Any>? {
        return this?.nullCast()
    }

    fun Any?.asAnyList(): List<Any> {
        return this.unsafeCast()
    }

    fun Any?.toAnyList(): List<Any>? {
        return this?.nullCast()
    }

    fun Any?.asAnyMap(): Map<Any, Any> {
        return this.unsafeCast()
    }

    fun Any?.toAnyMap(): Map<Any, Any>? {
        return this?.nullCast()
    }
}
