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

import de.bixilon.minosoft.modding.event.events.Event
import de.bixilon.minosoft.modding.event.listener.EventListener

interface AbstractEventMaster : Iterable<EventListener> {
    val size: Int

    fun fire(event: Event): Boolean

    fun <T : EventListener> register(invoker: T): T

    fun register(vararg invokers: EventListener) {
        for (invoker in invokers) {
            register(invoker)
        }
    }

    fun unregister(invoker: EventListener?) = Unit

    fun unregister(vararg invokers: EventListener?) {
        for (invoker in invokers) {
            unregister(invoker)
        }
    }
}
