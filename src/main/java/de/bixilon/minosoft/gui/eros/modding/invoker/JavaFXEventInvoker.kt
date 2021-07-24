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

package de.bixilon.minosoft.gui.eros.modding.invoker

import de.bixilon.minosoft.modding.event.events.CancelableEvent
import de.bixilon.minosoft.modding.event.events.Event
import de.bixilon.minosoft.modding.event.invoker.EventInstantFireable
import de.bixilon.minosoft.modding.event.invoker.EventInvoker
import de.bixilon.minosoft.modding.loading.Priorities
import javafx.application.Platform
import kotlin.reflect.KClass

/**
 * Basically a CallbackEventInvoker, bt the callback runs on the java fx ui thread
 */
class JavaFXEventInvoker<E : Event> private constructor(
    ignoreCancelled: Boolean,
    private val callback: (E) -> Unit,
    override val kEventType: KClass<out Event>,
    override val eventType: Class<out Event>,
    override val instantFire: Boolean,
) : EventInvoker(ignoreCancelled, Priorities.NORMAL, null), EventInstantFireable {

    override operator fun invoke(event: Event) {
        if (!this.isIgnoreCancelled && event is CancelableEvent && event.cancelled) {
            return
        }
        Platform.runLater {
            callback(event as E)
        }
    }

    companion object {
        @JvmOverloads
        @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
        inline fun <reified E : Event> of(ignoreCancelled: Boolean = false, instantFire: Boolean = true, noinline callback: (E) -> Unit): JavaFXEventInvoker<E> {
            return JavaFXEventInvoker(
                ignoreCancelled = ignoreCancelled,
                callback = callback,
                kEventType = E::class,
                eventType = E::class.java,
                instantFire = instantFire,
            )
        }
    }
}
