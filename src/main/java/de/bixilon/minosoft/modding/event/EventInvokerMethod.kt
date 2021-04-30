/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.modding.event

import de.bixilon.minosoft.modding.event.events.CancelableEvent
import de.bixilon.minosoft.modding.event.events.Event
import de.bixilon.minosoft.modding.event.events.annotations.EventHandler
import de.bixilon.minosoft.modding.loading.Priorities
import java.lang.reflect.Method

class EventInvokerMethod(
    ignoreCancelled: Boolean,
    priority: Priorities,
    listener: EventListener,
    val method: Method,
) : EventInvoker(ignoreCancelled, priority, listener) {
    override val eventType: Class<out Event> = method.parameters[0].type as Class<out Event>

    constructor(annotation: EventHandler, listener: EventListener, method: Method) : this(annotation.ignoreCancelled, annotation.priority, listener, method)

    override operator fun invoke(event: Event) {
        if (!method.parameters[0].type.isAssignableFrom(event.javaClass)) {
            return
        }
        if (!this.isIgnoreCancelled && event is CancelableEvent && event.isCancelled) {
            return
        }
        method(listener, event)
    }

}
