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
package de.bixilon.minosoft.modding.event.listener

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.modding.EventPriorities
import de.bixilon.minosoft.modding.event.events.CancelableEvent
import de.bixilon.minosoft.modding.event.events.Event
import de.bixilon.minosoft.modding.event.master.AbstractEventMaster
import kotlin.reflect.KClass

class CallbackEventListener<E : Event> constructor(
    ignoreCancelled: Boolean,
    private val callback: (E) -> Unit,
    override val kEventType: KClass<out Event>,
    override val eventType: Class<out Event>,
    priority: EventPriorities,
) : EventListener(ignoreCancelled, priority) {

    override operator fun invoke(event: Event) {
        if (!this.ignoreCancelled && event is CancelableEvent && event.cancelled) {
            return
        }
        callback(event.unsafeCast())
    }

    companion object {

        inline operator fun <reified E : Event> AbstractEventMaster.plusAssign(noinline callback: (E) -> Unit) {
            listen(callback = callback)
        }

        inline fun <reified E : Event> AbstractEventMaster.listen(ignoreCancelled: Boolean = false, priority: EventPriorities = EventPriorities.NORMAL, noinline callback: (E) -> Unit): CallbackEventListener<E> {
            val listener = of(ignoreCancelled, priority, callback)
            register(listener)
            return listener
        }

        inline fun <reified E : Event> of(ignoreCancelled: Boolean = false, priority: EventPriorities = EventPriorities.NORMAL, noinline callback: (E) -> Unit): CallbackEventListener<E> {
            return CallbackEventListener(
                ignoreCancelled = ignoreCancelled,
                callback = callback,
                kEventType = E::class,
                eventType = E::class.java,
                priority = priority,
            )
        }
    }
}
