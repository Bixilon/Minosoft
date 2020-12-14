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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.modding.event.events.annotations.Unsafe;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.ConnectionStates;

/**
 * Fired when the connection state was just changed
 */
@Unsafe
public class ConnectionStateChangeEvent extends ConnectionEvent {
    private final ConnectionStates previousState;
    private final ConnectionStates currentState;

    public ConnectionStateChangeEvent(Connection connection, ConnectionStates previousState, ConnectionStates currentState) {
        super(connection);
        this.previousState = previousState;
        this.currentState = currentState;
    }

    public ConnectionStates getPreviousState() {
        return this.previousState;
    }

    public ConnectionStates getCurrentState() {
        return this.currentState;
    }
}
