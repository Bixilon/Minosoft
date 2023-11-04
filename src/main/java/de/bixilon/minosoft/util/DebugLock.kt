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

package de.bixilon.minosoft.util

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.kutil.concurrent.lock.NobodyIsReadingException
import de.bixilon.kutil.concurrent.lock.NobodyIsWritingException


@Deprecated("kutil 1.25")
class DebugLock : Lock {
    private val lock = Object()
    override var readers = 0
        private set
    override var locked = false
        private set

    override fun acquire() {
        synchronized(lock) {
            while (locked) {
                lock.wait()
            }
            readers++
            //    Exception("acquire").printStackTrace()
            lock.notifyAll()
        }
    }

    override fun release() {
        synchronized(lock) {
            if (readers <= 0) throw NobodyIsReadingException()
            readers--
            //     Exception("release").printStackTrace()
            lock.notifyAll()
        }
    }

    override fun lock() {
        synchronized(lock) {
            while (locked || readers > 0) {
                lock.wait()
            }
            locked = true
            Exception("lock").printStackTrace()
            lock.notifyAll()
        }
    }

    override fun unlock() {
        synchronized(lock) {
            if (!locked) throw NobodyIsWritingException()
            locked = false
            Exception("unlock").printStackTrace()
            lock.notifyAll()
        }
    }
}
