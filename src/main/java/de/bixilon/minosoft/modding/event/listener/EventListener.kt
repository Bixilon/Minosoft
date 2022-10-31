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

import de.bixilon.minosoft.modding.EventPriorities
import de.bixilon.minosoft.modding.event.events.Event
import kotlin.reflect.KClass

abstract class EventListener(
    val ignoreCancelled: Boolean,
    val priority: EventPriorities,
) : Comparable<EventListener> {
    abstract val kEventType: KClass<out Event>?
    abstract val eventType: Class<out Event>

    abstract operator fun invoke(event: Event)

    override fun compareTo(other: EventListener): Int {
        return -(other.priority.ordinal - this.priority.ordinal)
    }
}
