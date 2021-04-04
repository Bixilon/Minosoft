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

import de.bixilon.minosoft.data.ChatTextPositions;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.data.player.Hands;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.modding.event.events.ChatMessageReceivingEvent;
import de.bixilon.minosoft.modding.event.events.ChatMessageSendingEvent;
import de.bixilon.minosoft.modding.event.events.CloseWindowEvent;
import de.bixilon.minosoft.modding.event.events.HeldItemChangeEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketLoginPluginResponse;
import de.bixilon.minosoft.protocol.packets.serverbound.play.*;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;
import org.checkerframework.common.value.qual.IntRange;

import java.util.UUID;

public class PacketSender {
    public static final String[] ILLEGAL_CHAT_CHARS = {"§"};
    private final PlayConnection connection;

    public PacketSender(PlayConnection connection) {
        this.connection = connection;
    }

    public void setFlyStatus(boolean flying) {
        this.connection.sendPacket(new PacketPlayerAbilitiesSending(flying));
    }

    public void sendChatMessage(String message) {
        for (String illegalChar : ILLEGAL_CHAT_CHARS) {
            if (message.contains(illegalChar)) {
                throw new IllegalArgumentException(String.format("%s is not allowed in chat", illegalChar));
            }
        }
        ChatMessageSendingEvent event = new ChatMessageSendingEvent(this.connection, message);
        if (this.connection.fireEvent(event)) {
            return;
        }
        Log.game("Sending chat message: %s", message);
        this.connection.sendPacket(new PacketChatMessageSending(event.getMessage()));
    }

    public void spectateEntity(UUID entityUUID) {
        this.connection.sendPacket(new PacketSpectate(entityUUID));
    }

    public void swingArm(Hands hand) {
        this.connection.sendPacket(new PacketAnimation(hand));
    }

    public void swingArm() {
        this.connection.sendPacket(new PacketAnimation(Hands.MAIN_HAND));
    }

    public void sendAction(PacketEntityAction.EntityActions action) {
        this.connection.sendPacket(new PacketEntityAction(this.connection.getWorld().getEntityIdMap().inverse().get(this.connection.getPlayer().getEntity()), action));
    }

    public void jumpWithHorse(int jumpBoost) {
        this.connection.sendPacket(new PacketEntityAction(this.connection.getWorld().getEntityIdMap().inverse().get(this.connection.getPlayer().getEntity()), PacketEntityAction.EntityActions.START_HORSE_JUMP, jumpBoost));
    }

    public void dropItem() {
        this.connection.sendPacket(new PacketPlayerDigging(PacketPlayerDigging.DiggingStatus.DROP_ITEM, null, PacketPlayerDigging.DiggingFaces.BOTTOM));
    }

    public void dropItemStack() {
        this.connection.sendPacket(new PacketPlayerDigging(PacketPlayerDigging.DiggingStatus.DROP_ITEM_STACK, null, PacketPlayerDigging.DiggingFaces.BOTTOM));
    }

    public void swapItemInHand() {
        this.connection.sendPacket(new PacketPlayerDigging(PacketPlayerDigging.DiggingStatus.SWAP_ITEMS_IN_HAND, null, PacketPlayerDigging.DiggingFaces.BOTTOM));
    }

    public void closeWindow(byte windowId) {
        CloseWindowEvent event = new CloseWindowEvent(this.connection, windowId, CloseWindowEvent.Initiators.CLIENT);
        if (this.connection.fireEvent(event)) {
            return;
        }
        this.connection.sendPacket(new PacketCloseWindowSending(windowId));
    }

    public void respawn() {
        sendClientStatus(PacketClientStatus.ClientStates.PERFORM_RESPAWN);
    }

    public void sendClientStatus(PacketClientStatus.ClientStates status) {
        this.connection.sendPacket(new PacketClientStatus(status));
    }

    public void sendPluginMessageData(String channel, OutByteBuffer toSend) {
        this.connection.sendPacket(new PacketPluginMessageSending(channel, toSend.toByteArray()));
    }

    public void sendPluginMessageData(ResourceLocation channel, OutByteBuffer toSend) {
        String channelName = channel.getFull();
        if (Util.doesStringContainsUppercaseLetters(channelName)) {
            channelName = channel.getPath();
        }
        this.connection.sendPacket(new PacketPluginMessageSending(channelName, toSend.toByteArray()));
    }

    public void sendLoginPluginMessageResponse(int messageId, OutByteBuffer toSend) {
        this.connection.sendPacket(new PacketLoginPluginResponse(messageId, toSend.toByteArray()));
    }

    public void setLocation(Vec3 position, EntityRotation rotation, boolean onGround) {
        this.connection.sendPacket(new PacketPlayerPositionAndRotationSending(position, rotation, onGround));
        this.connection.getPlayer().getEntity().setPosition(position);
        this.connection.getPlayer().getEntity().setRotation(rotation);
    }

    public void sendFakeChatMessage(ChatComponent message, ChatTextPositions position) {
        this.connection.fireEvent(new ChatMessageReceivingEvent(this.connection, message, position, null));
    }

    public void sendFakeChatMessage(String message) {
        sendFakeChatMessage(ChatComponent.valueOf(message), ChatTextPositions.CHAT_BOX);
    }

    public void selectSlot(@IntRange(from = 0, to = 8) int slot) {
        this.connection.fireEvent(new HeldItemChangeEvent(this.connection, slot));
        this.connection.getPlayer().getInventoryManager().setSelectedHotbarSlot(slot);
        this.connection.sendPacket(new PacketHeldItemChangeSending(slot));
    }
}
