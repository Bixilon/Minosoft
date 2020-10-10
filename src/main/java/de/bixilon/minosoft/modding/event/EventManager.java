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

import de.bixilon.minosoft.modding.event.events.Event;
import de.bixilon.minosoft.modding.event.events.annotations.EventHandler;
import de.bixilon.minosoft.util.ServerAddress;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class EventManager {
    private final HashSet<EventMethod> globalEventListeners = new HashSet<>();
    private final HashMap<HashSet<ServerAddress>, HashSet<EventMethod>> specificEventListeners = new HashMap<>();

    public void registerGlobalListener(EventListener listener) {
        globalEventListeners.addAll(getEventMethods(listener));
    }

    private HashSet<EventMethod> getEventMethods(EventListener listener) {
        Class<? extends EventListener> clazz = listener.getClass();
        HashSet<EventMethod> eventMethods = new HashSet<>();
        for (Method method : clazz.getMethods()) {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            if (annotation == null) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }
            if (!Event.class.isAssignableFrom(method.getParameters()[0].getType())) {
                continue;
            }
            eventMethods.add(new EventMethod(annotation, listener, method));
        }
        return eventMethods;
    }

    public HashSet<EventMethod> getGlobalEventListeners() {
        return globalEventListeners;
    }

    public void registerConnectionListener(EventListener listener, ServerAddress... addresses) {
        if (addresses.length == 0) {
            throw new RuntimeException("You must provide at least one server address or use global events!");
        }
        HashSet<ServerAddress> serverAddresses = new HashSet<>(Arrays.asList(addresses));
        specificEventListeners.put(serverAddresses, getEventMethods(listener));
    }

    public HashMap<HashSet<ServerAddress>, HashSet<EventMethod>> getSpecificEventListeners() {
        return specificEventListeners;
    }
}
