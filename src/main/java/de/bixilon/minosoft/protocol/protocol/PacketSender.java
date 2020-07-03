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

package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketChatMessage;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketHeldItemChangeSending;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketPlayerAbilitiesSending;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketSpectate;

import java.util.UUID;

public class PacketSender {
    final Connection connection;

    public PacketSender(Connection connection) {
        this.connection = connection;
    }

    public void setFlyStatus(boolean flying) {
        connection.sendPacket(new PacketPlayerAbilitiesSending(flying));
    }

    public void sendChatMessage(String message) {
        connection.sendPacket(new PacketChatMessage(message));
    }

    public void spectateEntity(UUID entityUUID) {
        connection.sendPacket(new PacketSpectate(entityUUID));
    }

    public void setSlot(int slotId) {
        connection.sendPacket(new PacketHeldItemChangeSending(slotId));
    }
}
