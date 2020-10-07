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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.game.datatypes.ChatTextPositions;
import de.bixilon.minosoft.game.datatypes.text.BaseComponent;
import de.bixilon.minosoft.modding.event.EventListener;
import de.bixilon.minosoft.modding.event.events.annotations.MinimumProtocolVersion;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketChatMessageReceiving;

import java.util.UUID;

public class ChatMessageReceivingEvent extends CancelableEvent {
    private final BaseComponent message;
    private final ChatTextPositions position;
    private final UUID sender;

    public ChatMessageReceivingEvent(Connection connection, BaseComponent message, ChatTextPositions position, UUID sender) {
        super(connection);
        this.message = message;
        this.position = position;
        this.sender = sender;
    }

    public ChatMessageReceivingEvent(Connection connection, PacketChatMessageReceiving pkg) {
        super(connection);
        this.message = pkg.getMessage();
        this.position = pkg.getPosition();
        this.sender = pkg.getSender();
    }

    public BaseComponent getMessage() {
        return message;
    }

    public ChatTextPositions getPosition() {
        return position;
    }

    /**
     * @return The uuid of the sender
     */
    @MinimumProtocolVersion(protocolId = 718)
    public UUID getSender() {
        return this.sender;
    }

    @Override
    public void handle(EventListener listener) {
        listener.onChatMessageReceiving(this);
    }
}
