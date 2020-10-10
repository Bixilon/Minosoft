/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.event;

import de.bixilon.minosoft.modding.event.events.CancelableEvent;
import de.bixilon.minosoft.modding.event.events.Event;
import de.bixilon.minosoft.modding.event.events.annotations.EventHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EventMethod {
    private final EventHandler annotation;
    private final EventListener listener;
    private final Method method;

    public EventMethod(EventHandler annotation, EventListener listener, Method method) {
        this.annotation = annotation;
        this.listener = listener;
        this.method = method;
    }

    public EventHandler getAnnotation() {
        return annotation;
    }

    public Method getMethod() {
        return method;
    }

    public void invoke(Event event) {
        if (!method.getParameters()[0].getType().isAssignableFrom(event.getClass())) {
            return;
        }
        if (annotation.onlyIfNotCancelled() && event instanceof CancelableEvent && ((CancelableEvent) event).isCancelled()) {
            return;
        }
        try {
            method.invoke(listener, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
