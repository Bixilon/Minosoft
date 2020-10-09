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

package de.bixilon.minosoft.modding.event;

import de.bixilon.minosoft.modding.event.events.*;
import de.bixilon.minosoft.modding.event.events.annotations.Unsafe;

public class EventListener {
    public void onChatMessageReceiving(ChatMessageReceivingEvent event) {
    }

    public void onChatMessageSending(ChatMessageSendingEvent event) {
    }

    public void onLoginDisconnect(LoginDisconnectEvent event) {
    }

    public void onDisconnect(DisconnectEvent event) {
    }

    public void onResourcePackChange(ResourcePackChangeEvent event) {
    }

    public void onBlockBreakAnimation(BlockBreakAnimationEvent event) {
    }

    public void onHealthChange(UpdateHealthEvent event) {
    }

    public void onOpenSignEditor(OpenSignEditorEvent event) {
    }

    @Unsafe
    public void onPacketSend(PacketSendEvent event) {
    }

    @Unsafe
    public void onPacketReceive(PacketReceiveEvent event) {
    }

    public void onPongEvent(StatusPongEvent event) {
    }

    public void onStatusResponse(StatusResponseEvent event) {
    }

    public void onBlockAction(BlockActionEvent event) {
    }

    public void onBlockChange(BlockChangeEvent event) {
    }

    public void onBossBarChange(BossBarChangeEvent event) {
    }

    public void onEntitySpectate(EntitySpectateEvent event) {
    }

    public void onChangeGameState(ChangeGameStateEvent event) {
    }

    public void onWindowClose(CloseWindowEvent event) {
    }

    public void onItemCollectAnimation(CollectItemAnimationEvent event) {
    }

    public void onEntityDespawn(EntityDespawnEvent event) {
    }

    public void onEntityEquipmentChange(EntityEquipmentChangeEvent event) {
    }

    public void onLightningBoltSpawn(LightningBoltSpawnEvent event) {
    }

    public void onMultiBlockChange(MultiBlockChangeEvent event) {
    }

    public void onBlockEntityMetaDataChange(BlockEntityMetaDataChangeEvent event) {
    }

    public void onChunkDataChange(ChunkDataChangeEvent event) {
    }

    public void onEffect(EffectEvent event) {
    }
}
