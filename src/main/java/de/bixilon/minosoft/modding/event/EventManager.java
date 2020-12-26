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

import de.bixilon.minosoft.modding.event.address.ServerAddressValidator;
import de.bixilon.minosoft.modding.event.events.ConnectionEvent;
import de.bixilon.minosoft.modding.event.events.annotations.EventHandler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class EventManager {
    private final HashSet<EventInvoker> globalEventListeners = new HashSet<>();
    private final HashMap<HashSet<ServerAddressValidator>, HashSet<EventInvoker>> specificEventListeners = new HashMap<>();

    public void registerGlobalListener(EventListener listener) {
        this.globalEventListeners.addAll(getEventMethods(listener));
    }

    private HashSet<EventInvoker> getEventMethods(EventListener listener) {
        Class<? extends EventListener> clazz = listener.getClass();
        HashSet<EventInvoker> eventInvokers = new HashSet<>();
        for (Method method : clazz.getMethods()) {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            if (annotation == null) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }
            if (!ConnectionEvent.class.isAssignableFrom(method.getParameters()[0].getType())) {
                continue;
            }
            eventInvokers.add(new EventInvokerMethod(annotation, listener, method));
        }
        return eventInvokers;
    }

    public HashSet<EventInvoker> getGlobalEventListeners() {
        return this.globalEventListeners;
    }

    public void registerConnectionListener(EventListener listener, ServerAddressValidator... addresses) {
        if (addresses.length == 0) {
            throw new RuntimeException("You must provide at least one address validator or use global events!");
        }
        HashSet<ServerAddressValidator> serverAddresses = new HashSet<>(Arrays.asList(addresses));
        this.specificEventListeners.put(serverAddresses, getEventMethods(listener));
    }

    public HashMap<HashSet<ServerAddressValidator>, HashSet<EventInvoker>> getSpecificEventListeners() {
        return this.specificEventListeners;
    }
}
