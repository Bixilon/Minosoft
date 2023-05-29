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

package de.bixilon.minosoft.modding.event.master

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedList
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.modding.event.events.AsyncEvent
import de.bixilon.minosoft.modding.event.events.CancelableEvent
import de.bixilon.minosoft.modding.event.events.Event
import de.bixilon.minosoft.modding.event.listener.EventListener
import de.bixilon.minosoft.modding.event.listener.OneShotListener
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import java.util.*

open class EventMaster(vararg parents: AbstractEventMaster) : AbstractEventMaster {
    private val parents: MutableSet<AbstractEventMaster> = mutableSetOf(*parents)
    private val parentLock = SimpleLock()
    private val eventListeners: PriorityQueue<EventListener> = PriorityQueue()
    private val eventInvokerLock = SimpleLock()

    override val size: Int
        get() {
            var size = eventListeners.size
            parentLock.acquire()
            for (parent in parents) {
                size += parent.size
            }
            parentLock.release()
            return size
        }

    private fun runEvent(invoker: EventListener, event: Event, toRemove: MutableSet<EventListener>) {
        try {
            invoker(event)
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }

        if (invoker is OneShotListener && invoker.oneShot) {
            toRemove += invoker
        }
    }


    override fun fire(event: Event): Boolean {
        parentLock.acquire()
        if (parents.isNotEmpty()) {
            for (parent in parents) {
                parent.fire(event)
            }
        }
        parentLock.release()

        val toRemove: MutableSet<EventListener> = ObjectOpenHashSet()
        eventInvokerLock.acquire()
        val worker = UnconditionalWorker()
        for (invoker in eventListeners) {
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
            this.eventListeners -= toRemove
            eventInvokerLock.unlock()
        }


        if (event is CancelableEvent) {
            val cancelled = event.cancelled
            event.cancelled = false // Cleanup memory
            return cancelled
        }
        return false
    }

    override fun unregister(invoker: EventListener?) {
        eventInvokerLock.lock()
        try {
            eventListeners -= invoker ?: return
        } finally {
            eventInvokerLock.unlock()
        }
    }

    override fun <T : EventListener> register(invoker: T): T {
        eventInvokerLock.lock()
        eventListeners += invoker
        eventInvokerLock.unlock()
        return invoker
    }


    override fun iterator(): Iterator<EventListener> {
        return eventListeners.toSynchronizedList().iterator()
    }
}
