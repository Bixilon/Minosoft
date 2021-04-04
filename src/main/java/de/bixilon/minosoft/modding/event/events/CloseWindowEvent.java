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

import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketCloseWindowReceiving;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketCloseWindowSending;

/**
 * Fired when a inventory (window) closes
 */
public class CloseWindowEvent extends CancelableEvent {
    private final byte windowId;
    private final Initiators initiator;

    public CloseWindowEvent(PlayConnection connection, byte windowId, Initiators initiator) {
        super(connection);
        this.windowId = windowId;
        this.initiator = initiator;
    }

    public CloseWindowEvent(PlayConnection connection, PacketCloseWindowReceiving pkg) {
        super(connection);
        this.windowId = pkg.getWindowId();
        this.initiator = Initiators.SERVER;
    }

    public CloseWindowEvent(PlayConnection connection, PacketCloseWindowSending pkg) {
        super(connection);
        this.windowId = pkg.getWindowId();
        this.initiator = Initiators.CLIENT;
    }

    public byte getWindowId() {
        return this.windowId;
    }

    public Initiators getInitiator() {
        return this.initiator;
    }

    public enum Initiators {
        CLIENT,
        SERVER
    }
}
