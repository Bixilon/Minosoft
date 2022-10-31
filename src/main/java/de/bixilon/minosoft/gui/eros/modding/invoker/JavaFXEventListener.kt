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

package de.bixilon.minosoft.gui.eros.modding.invoker

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.modding.EventPriorities
import de.bixilon.minosoft.modding.event.events.CancelableEvent
import de.bixilon.minosoft.modding.event.events.Event
import de.bixilon.minosoft.modding.event.listener.EventInstantFireable
import de.bixilon.minosoft.modding.event.listener.EventListener
import de.bixilon.minosoft.modding.event.listener.OneShotListener
import de.bixilon.minosoft.modding.event.master.AbstractEventMaster
import kotlin.reflect.KClass

/**
 * Basically a CallbackEventInvoker, bt the callback runs on the java fx ui thread
 */
class JavaFXEventListener<E : Event> constructor(
    ignoreCancelled: Boolean,
    private val callback: (E) -> Unit,
    override val oneShot: Boolean,
    override val kEventType: KClass<out Event>,
    override val eventType: Class<out Event>,
    override val instantFire: Boolean,
) : EventListener(ignoreCancelled, EventPriorities.NORMAL), EventInstantFireable, OneShotListener {

    override operator fun invoke(event: Event) {
        if (!this.ignoreCancelled && event is CancelableEvent && event.cancelled) {
            return
        }
        JavaFXUtil.runLater {
            callback(event.unsafeCast())
        }
    }

    companion object {

        inline fun <reified E : Event> AbstractEventMaster.javaFX(ignoreCancelled: Boolean = false, instantFire: Boolean = true, oneShot: Boolean = false, noinline callback: (E) -> Unit): JavaFXEventListener<E> {
            val listener = of(ignoreCancelled, instantFire, oneShot, callback)
            register(listener)
            return listener
        }

        inline fun <reified E : Event> of(ignoreCancelled: Boolean = false, instantFire: Boolean = true, oneShot: Boolean = false, noinline callback: (E) -> Unit): JavaFXEventListener<E> {
            return JavaFXEventListener(
                ignoreCancelled = ignoreCancelled,
                callback = callback,
                oneShot = oneShot,
                kEventType = E::class,
                eventType = E::class.java,
                instantFire = instantFire,
            )
        }
    }
}
