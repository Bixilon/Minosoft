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

import de.bixilon.minosoft.modding.event.events.ConnectionEvent;
import de.bixilon.minosoft.modding.loading.Priorities;

public class EventInvokerCallback<V extends ConnectionEvent> extends EventInvoker {
    private final InvokerCallback<V> callback;

    Class<? extends ConnectionEvent> eventType = ConnectionEvent.class;

    public EventInvokerCallback(boolean ignoreCancelled, InvokerCallback<V> callback) {
        super(ignoreCancelled, Priorities.NORMAL, null);
        this.callback = callback;
    }

    // if you need instant fireing support
    public EventInvokerCallback(Class<? extends ConnectionEvent> eventType, InvokerCallback<V> callback) {
        this(false, callback);
        this.eventType = eventType; // ToDo: how to get the class of V? seems to be impossible
    }

    @SuppressWarnings("unchecked")
    public void invoke(ConnectionEvent event) {
        if (this.eventType != event.getClass()) {
            return;
        }
        this.callback.handle((V) event);
    }

    public Class<? extends ConnectionEvent> getEventType() {
        return this.eventType;
    }

    public interface InvokerCallback<V> {
        void handle(V event);
    }
}
