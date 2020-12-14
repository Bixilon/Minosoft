/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.event;

import de.bixilon.minosoft.modding.event.events.CancelableEvent;
import de.bixilon.minosoft.modding.event.events.ConnectionEvent;
import de.bixilon.minosoft.modding.event.events.annotations.EventHandler;
import de.bixilon.minosoft.modding.loading.Priorities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EventInvokerMethod extends EventInvoker {
    private final Method method;
    private final Class<? extends ConnectionEvent> eventType;

    @SuppressWarnings("unchecked")
    public EventInvokerMethod(boolean ignoreCancelled, Priorities priority, EventListener listener, Method method) {
        super(ignoreCancelled, priority, listener);
        this.method = method;
        this.eventType = (Class<? extends ConnectionEvent>) method.getParameters()[0].getType();
    }

    public EventInvokerMethod(EventHandler annotation, EventListener listener, Method method) {
        this(annotation.ignoreCancelled(), annotation.priority(), listener, method);
    }

    public Method getMethod() {
        return this.method;
    }

    public void invoke(ConnectionEvent event) {
        if (!this.method.getParameters()[0].getType().isAssignableFrom(event.getClass())) {
            return;
        }
        if (!this.ignoreCancelled && event instanceof CancelableEvent cancelableEvent && cancelableEvent.isCancelled()) {
            return;
        }
        try {
            this.method.invoke(this.listener, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Class<? extends ConnectionEvent> getEventType() {
        return this.eventType;
    }
}
