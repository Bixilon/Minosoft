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

package de.bixilon.minosoft.util

import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.time.TimeUtil

class Previous<T>(value: T, private val interpolator: ((previous: Previous<T>, delta: Long) -> T)? = null) {
    private val changeListeners: MutableSet<(value: T, previous: T) -> Unit> = synchronizedSetOf()
    private var lastChangeTime = TimeUtil.millis
    var value: T = value
        @Synchronized
        set(value) {
            previous = field
            field = value
            lastChangeTime = TimeUtil.millis
            for (listener in changeListeners) {
                listener(value, previous)
            }
        }
    var previous: T = value

    fun addChangeListener(invoker: (value: T, previous: T) -> Unit) {
        changeListeners += invoker
    }

    operator fun plusAssign(invoker: (value: T, previous: T) -> Unit) {
        addChangeListener(invoker)
    }

    fun assign() {
        previous = value
    }

    fun equals(): Boolean {
        return value == previous
    }

    fun interpolate(): T {
        return interpolator!!(this, (TimeUtil.millis - lastChangeTime))
    }

}
