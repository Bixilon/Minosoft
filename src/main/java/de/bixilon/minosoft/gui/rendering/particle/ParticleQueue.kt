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
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.minosoft.gui.rendering.particle.types.Particle

class ParticleQueue(val renderer: ParticleRenderer) {
    private val lock = Lock.lock()
    private val queue: ArrayDeque<Particle> = ArrayDeque(QUEUE_CAPACITY)


    operator fun plusAssign(particle: Particle) = queue(particle)
    fun queue(particle: Particle) {
        if (queue.size > QUEUE_CAPACITY || renderer.size + queue.size > renderer.maxAmount) return
        lock.locked { queue += particle }
    }


    fun clear() = lock.locked {
        queue.clear()
    }

    fun addTo(list: MutableList<Particle>) {
        if (queue.isEmpty()) return
        lock.lock()

        while (queue.isNotEmpty() && list.size < renderer.maxAmount) {
            list.add(queue.removeFirst())
        }

        lock.unlock()
    }


    companion object {
        const val QUEUE_CAPACITY = 1000
    }
}
