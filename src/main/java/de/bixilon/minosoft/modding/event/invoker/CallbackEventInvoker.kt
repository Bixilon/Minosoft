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
package de.bixilon.minosoft.modding.event.invoker

import de.bixilon.minosoft.modding.event.events.CancelableEvent
import de.bixilon.minosoft.modding.event.events.Event
import de.bixilon.minosoft.modding.loading.Priorities
import de.bixilon.minosoft.util.KUtil.unsafeCast
import kotlin.reflect.KClass

class CallbackEventInvoker<E : Event> private constructor(
    ignoreCancelled: Boolean,
    private val callback: (E) -> Unit,
    override val kEventType: KClass<out Event>,
    override val eventType: Class<out Event>,
    override val instantFire: Boolean,
    priority: Priorities,
) : EventInvoker(ignoreCancelled, priority, null), EventInstantFireable {

    override operator fun invoke(event: Event) {
        if (!this.isIgnoreCancelled && event is CancelableEvent && event.cancelled) {
            return
        }
        callback(event.unsafeCast())
    }

    companion object {
        @JvmOverloads
        @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
        inline fun <reified E : Event> of(ignoreCancelled: Boolean = false, instantFire: Boolean = true, priority: Priorities = Priorities.NORMAL, noinline callback: (E) -> Unit): CallbackEventInvoker<E> {
            return CallbackEventInvoker(
                ignoreCancelled = ignoreCancelled,
                callback = callback,
                kEventType = E::class,
                eventType = E::class.java,
                instantFire = instantFire,
                priority = priority,
            )
        }
    }
}
