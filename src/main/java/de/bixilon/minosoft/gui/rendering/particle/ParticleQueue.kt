/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.minosoft.gui.rendering.particle.types.Particle

class ParticleQueue(val renderer: ParticleRenderer) {
    private val lock = Lock.lock()
    private val queue: MutableList<Particle> = ArrayList(QUEUE_CAPACITY)


    operator fun plusAssign(particle: Particle) = queue(particle)
    fun queue(particle: Particle) {
        lock.lock()
        val size = queue.size
        if (size > QUEUE_CAPACITY || renderer.size + size > renderer.maxAmount) {
            // already overloaded, ignore
            lock.unlock()
            return
        }
        queue += particle
        lock.unlock()
    }


    fun clear() {
        lock.lock()
        queue.clear()
        lock.unlock()
    }

    fun add(list: MutableList<Particle>) {
        if (queue.isEmpty()) return
        lock.lock()

        while (queue.isNotEmpty() && list.size < renderer.maxAmount) {
            list.add(queue.removeAt(0))
        }

        lock.unlock()
    }


    companion object {
        const val QUEUE_CAPACITY = 1000
    }
}
