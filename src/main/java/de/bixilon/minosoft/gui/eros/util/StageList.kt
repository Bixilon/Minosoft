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

package de.bixilon.minosoft.gui.eros.util

import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import javafx.stage.Stage
import java.lang.ref.WeakReference

class StageList : Iterable<Stage?> {
    private val stages: MutableList<WeakReference<Stage>> = mutableListOf()
    val lock = SimpleLock()

    private fun <T> MutableList<WeakReference<T>>.cleanup() {
        val iterator = this.iterator()
        for (reference in iterator) {
            if (reference.get() != null) {
                continue
            }
            iterator.remove()
        }
    }


    fun cleanup() {
        lock.lock()
        stages.cleanup()
        lock.unlock()
    }

    fun add(stage: Stage) {
        lock.lock()
        stages += WeakReference(stage)
        lock.unlock()
    }

    override fun iterator(): Iterator<Stage?> {
        return object : Iterator<Stage?> {
            private val iterator = stages.iterator()

            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun next(): Stage? {
                while (iterator.hasNext()) {
                    val entry = iterator.next().get()
                    if (entry != null) {
                        return entry
                    }
                    iterator.remove()
                }
                return null
            }

        }
    }
}
