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

package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.minosoft.data.player.Hands;
import de.bixilon.minosoft.modding.event.events.ChatMessageSendingEvent;
import de.bixilon.minosoft.modding.event.events.CloseWindowEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketLoginPluginResponse;
import de.bixilon.minosoft.protocol.packets.serverbound.play.*;

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
        ChatMessageSendingEvent event = new ChatMessageSendingEvent(connection, message);
        if (connection.fireEvent(event)) {
            return;
        }
        connection.sendPacket(new PacketChatMessageSending(event.getMessage()));
    }

    public void spectateEntity(UUID entityUUID) {
        connection.sendPacket(new PacketSpectate(entityUUID));
    }

    public void setSlot(int slotId) {
        connection.sendPacket(new PacketHeldItemChangeSending(slotId));
    }

    public void swingArm(Hands hand) {
        connection.sendPacket(new PacketAnimation(hand));
    }

    public void swingArm() {
        connection.sendPacket(new PacketAnimation(Hands.MAIN_HAND));
    }

    public void sendAction(PacketEntityAction.EntityActions action) {
        // connection.sendPacket(new PacketEntityAction(connection.getPlayer().getPlayer().getEntityId(), action));
    }

    public void jumpWithHorse(int jumpBoost) {
        // connection.sendPacket(new PacketEntityAction(connection.getPlayer().getPlayer().getEntityId(), PacketEntityAction.EntityActions.START_HORSE_JUMP, jumpBoost));
    }

    public void dropItem() {
        connection.sendPacket(new PacketPlayerDigging(PacketPlayerDigging.DiggingStatus.DROP_ITEM, null, PacketPlayerDigging.DiggingFaces.BOTTOM));
    }

    public void dropItemStack() {
        connection.sendPacket(new PacketPlayerDigging(PacketPlayerDigging.DiggingStatus.DROP_ITEM_STACK, null, PacketPlayerDigging.DiggingFaces.BOTTOM));
    }

    public void swapItemInHand() {
        connection.sendPacket(new PacketPlayerDigging(PacketPlayerDigging.DiggingStatus.SWAP_ITEMS_IN_HAND, null, PacketPlayerDigging.DiggingFaces.BOTTOM));
    }

    public void closeWindow(byte windowId) {
        CloseWindowEvent event = new CloseWindowEvent(connection, windowId, CloseWindowEvent.Initiators.CLIENT);
        if (connection.fireEvent(event)) {
            return;
        }
        connection.sendPacket(new PacketCloseWindowSending(windowId));
    }

    public void respawn() {
        sendClientStatus(PacketClientStatus.ClientStates.PERFORM_RESPAWN);
    }

    public void sendClientStatus(PacketClientStatus.ClientStates status) {
        connection.sendPacket(new PacketClientStatus(status));
    }

    public void sendPluginMessageData(String channel, OutByteBuffer toSend) {
        connection.sendPacket(new PacketPluginMessageSending(channel, toSend.getOutBytes()));
    }

    public void sendLoginPluginMessageResponse(int messageId, OutByteBuffer toSend) {
        connection.sendPacket(new PacketLoginPluginResponse(messageId, toSend.getOutBytes()));
    }
}
