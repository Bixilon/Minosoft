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

import de.bixilon.minosoft.data.ChatTextPositions;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.modding.event.events.annotations.MinimumProtocolVersion;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketChatMessageReceiving;

import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W21A;

public class ChatMessageReceivingEvent extends CancelableEvent {
    private final ChatComponent message;
    private final ChatTextPositions position;
    private final UUID sender;

    public ChatMessageReceivingEvent(PlayConnection connection, ChatComponent message, ChatTextPositions position, UUID sender) {
        super(connection);
        this.message = message;
        this.position = position;
        this.sender = sender;
    }

    public ChatMessageReceivingEvent(PlayConnection connection, PacketChatMessageReceiving pkg) {
        super(connection);
        this.message = pkg.getMessage();
        this.position = pkg.getPosition();
        this.sender = pkg.getSender();
    }

    public ChatComponent getMessage() {
        return this.message;
    }

    public ChatTextPositions getPosition() {
        return this.position;
    }

    /**
     * @return The uuid of the sender
     */
    @MinimumProtocolVersion(versionId = V_20W21A)
    public UUID getSender() {
        return this.sender;
    }
}
