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

import de.bixilon.minosoft.game.datatypes.player.Hand;
import de.bixilon.minosoft.protocol.network.Connection;
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
        connection.sendPacket(new PacketChatMessage(message));
    }

    public void spectateEntity(UUID entityUUID) {
        connection.sendPacket(new PacketSpectate(entityUUID));
    }

    public void setSlot(int slotId) {
        connection.sendPacket(new PacketHeldItemChangeSending(slotId));
    }

    public void swingArm(Hand hand) {
        connection.sendPacket(new PacketAnimation(hand));
    }

    public void swingArm() {
        connection.sendPacket(new PacketAnimation(Hand.RIGHT));
    }

    public void sendAction(PacketEntityAction.EntityActions action) {
        connection.sendPacket(new PacketEntityAction(connection.getPlayer().getPlayer().getEntityId(), action));
    }

    public void jumpWithHorse(int jumpBoost) {
        connection.sendPacket(new PacketEntityAction(connection.getPlayer().getPlayer().getEntityId(), PacketEntityAction.EntityActions.START_HORSE_JUMP, jumpBoost));
    }

    public void dropItem() {
        connection.sendPacket(new PacketPlayerDigging(PacketPlayerDigging.DiggingStatus.DROP_ITEM, null, PacketPlayerDigging.DiggingFace.BOTTOM));
    }

    public void dropItemStack() {
        connection.sendPacket(new PacketPlayerDigging(PacketPlayerDigging.DiggingStatus.DROP_ITEM_STACK, null, PacketPlayerDigging.DiggingFace.BOTTOM));
    }

    public void swapItemInHand() {
        connection.sendPacket(new PacketPlayerDigging(PacketPlayerDigging.DiggingStatus.SWAP_ITEMS_IN_HAND, null, PacketPlayerDigging.DiggingFace.BOTTOM));
    }

    public void closeWindow(byte windowId) {
        connection.sendPacket(new PacketCloseWindowSending(windowId));
    }

    public void sendClientStatus(PacketClientStatus.ClientStatus status) {
        connection.sendPacket(new PacketClientStatus(status));
    }

    public void respawn() {
        sendClientStatus(PacketClientStatus.ClientStatus.PERFORM_RESPAWN);
    }
}
