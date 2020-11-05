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

import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginPluginRequest;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

public class LoginPluginMessageRequestEvent extends CancelableEvent {
    private final int messageId;
    private final String channel;
    private final InByteBuffer data;

    public LoginPluginMessageRequestEvent(Connection connection, int messageId, String channel, InByteBuffer data) {
        super(connection);
        this.messageId = messageId;
        this.channel = channel;
        this.data = data;
    }

    public LoginPluginMessageRequestEvent(Connection connection, PacketLoginPluginRequest pkg) {
        super(connection);
        this.messageId = pkg.getMessageId();
        this.channel = pkg.getChannel();
        this.data = pkg.getDataAsBuffer();
    }

    public int getMessageId() {
        return messageId;
    }

    public String getChannel() {
        return channel;
    }

    public InByteBuffer getData() {
        return data;
    }
}
