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

package de.bixilon.minosoft.data.entities.data

import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.observer.ObserveUtil.invalid
import de.bixilon.kutil.observer.RemoveObserver

class EntityDataDelegate<V>(default: V) : DataObserver<V>(default) {

    @Deprecated("kutil 1.25")
    fun set(value: V) {
        if (this.value == value) return
        this.value = value
        lock.lock()
        val iterator = observers.iterator()
        for ((reference, _, observer) in iterator) {
            if (reference.invalid) {
                iterator.remove()
                continue
            }
            try {
                observer(value)
            } catch (_: RemoveObserver) {
                iterator.remove()
                continue
            } catch (exception: Throwable) {
                exception.printStackTrace()
            }
        }
        lock.unlock()
    }

}
