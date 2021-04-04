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

import de.bixilon.minosoft.modding.event.events.Event;
import de.bixilon.minosoft.modding.loading.Priorities;

public abstract class EventInvoker {
    protected final EventListener listener;
    protected final boolean ignoreCancelled;
    protected final Priorities priority;

    public EventInvoker(boolean ignoreCancelled, Priorities priority, EventListener listener) {
        this.ignoreCancelled = ignoreCancelled;
        this.listener = listener;
        this.priority = priority;
    }

    public boolean isIgnoreCancelled() {
        return this.ignoreCancelled;
    }

    public Priorities getPriority() {
        return this.priority;
    }

    public abstract void invoke(Event event);

    public abstract Class<? extends Event> getEventType();
}
