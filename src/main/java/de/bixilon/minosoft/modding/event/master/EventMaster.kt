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

package de.bixilon.minosoft.modding.event.master

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedList
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.modding.event.EventInstantFire
import de.bixilon.minosoft.modding.event.events.AsyncEvent
import de.bixilon.minosoft.modding.event.events.CancelableEvent
import de.bixilon.minosoft.modding.event.events.Event
import de.bixilon.minosoft.modding.event.invoker.EventInstantFireable
import de.bixilon.minosoft.modding.event.invoker.EventInvoker
import de.bixilon.minosoft.modding.event.invoker.OneShotInvoker
import java.util.*
import kotlin.reflect.full.companionObjectInstance

open class EventMaster(vararg parents: AbstractEventMaster) : AbstractEventMaster {
    private val parents: MutableSet<AbstractEventMaster> = mutableSetOf(*parents)
    private val parentLock = SimpleLock()
    private val eventInvokers: PriorityQueue<EventInvoker> = PriorityQueue()
    private val eventInvokerLock = SimpleLock()

    override val size: Int
        get() {
            var size = eventInvokers.size
            parentLock.acquire()
            for (parent in parents) {
                size += parent.size
            }
            parentLock.release()
            return size
        }

    private fun runEvent(invoker: EventInvoker, event: Event, toRemove: MutableSet<EventInvoker>) {
        try {
            invoker(event)
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }

        if (invoker is OneShotInvoker && invoker.oneShot) {
            toRemove += invoker
        }
    }


    override fun fireEvent(event: Event): Boolean {
        parentLock.acquire()
        for (parent in parents) {
            parent.fireEvent(event)
        }
        parentLock.release()

        val toRemove: MutableSet<EventInvoker> = mutableSetOf()
        eventInvokerLock.acquire()
        val worker = UnconditionalWorker()
        for (invoker in eventInvokers) {
            if (!invoker.eventType.isAssignableFrom(event::class.java)) {
                continue
            }
            if (event is AsyncEvent) {
                worker += { runEvent(invoker, event, toRemove) }
            } else {
                runEvent(invoker, event, toRemove)
            }
        }
        eventInvokerLock.release()
        if (event is AsyncEvent) {
            worker.work(CountUpAndDownLatch(0))
        }
        if (toRemove.isNotEmpty()) {
            eventInvokerLock.lock()
            this.eventInvokers -= toRemove
            eventInvokerLock.unlock()
        }


        if (event is CancelableEvent) {
            val cancelled = event.cancelled
            event.cancelled = false // Cleanup memory
            return cancelled
        }
        return false
    }

    override fun unregisterEvent(invoker: EventInvoker?) {
        eventInvokerLock.lock()
        try {
            eventInvokers -= invoker ?: return
        } finally {
            eventInvokerLock.unlock()
        }
    }

    override fun <T : EventInvoker> registerEvent(invoker: T): T {
        eventInvokerLock.lock()
        eventInvokers += invoker
        eventInvokerLock.unlock()

        if (invoker is EventInstantFireable && invoker.instantFire) {
            val companion = invoker.kEventType?.companionObjectInstance ?: return invoker

            if (companion is EventInstantFire<*>) {
                invoker.invoke(companion.fire())
            }
        }
        return invoker
    }


    override fun iterator(): Iterator<EventInvoker> {
        return eventInvokers.toSynchronizedList().iterator()
    }
}
