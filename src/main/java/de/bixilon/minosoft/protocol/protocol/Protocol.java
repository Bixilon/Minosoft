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

import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketEncryptionKeyRequest;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginDisconnect;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginSuccess;
import de.bixilon.minosoft.protocol.packets.clientbound.play.*;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusPong;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusResponse;

import java.util.HashMap;

public interface Protocol {
    HashMap<Packets.Clientbound, Class<? extends ClientboundPacket>> packetClassMapping = new HashMap<>();

    static Class<? extends ClientboundPacket> getPacketByPacket(Packets.Clientbound p) {
        if (packetClassMapping.size() == 0) {
            // init
            initPacketClassMapping();
        }
        return packetClassMapping.get(p);
    }

    private static void initPacketClassMapping() {
        packetClassMapping.put(Packets.Clientbound.STATUS_RESPONSE, PacketStatusResponse.class);
        packetClassMapping.put(Packets.Clientbound.STATUS_PONG, PacketStatusPong.class);
        packetClassMapping.put(Packets.Clientbound.LOGIN_ENCRYPTION_REQUEST, PacketEncryptionKeyRequest.class);
        packetClassMapping.put(Packets.Clientbound.LOGIN_LOGIN_SUCCESS, PacketLoginSuccess.class);
        packetClassMapping.put(Packets.Clientbound.LOGIN_DISCONNECT, PacketLoginDisconnect.class);

        packetClassMapping.put(Packets.Clientbound.PLAY_JOIN_GAME, PacketJoinGame.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_PLAYER_INFO, PacketPlayerInfo.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_TIME_UPDATE, PacketTimeUpdate.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_KEEP_ALIVE, PacketKeepAlive.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CHUNK_BULK, PacketChunkBulk.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_UPDATE_HEALTH, PacketUpdateHealth.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_PLUGIN_MESSAGE, PacketPluginMessageReceiving.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_POSITION, PacketSpawnLocation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CHAT_MESSAGE, PacketChatMessage.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_DISCONNECT, PacketDisconnect.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_HELD_ITEM_CHANGE, PacketHeldItemChangeReceiving.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SET_EXPERIENCE, PacketSetExperience.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CHANGE_GAME_STATE, PacketChangeGameState.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_MOB, PacketSpawnMob.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_POSITION_AND_ROTATION, PacketEntityPositionAndRotation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_POSITION, PacketEntityPosition.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_ROTATION, PacketEntityRotation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_DESTROY_ENTITIES, PacketDestroyEntity.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_VELOCITY, PacketEntityVelocity.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_PLAYER, PacketSpawnPlayer.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_TELEPORT, PacketEntityTeleport.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_HEAD_LOOK, PacketEntityHeadRotation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_WINDOW_ITEMS, PacketWindowItems.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_METADATA, PacketEntityMetadata.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_EQUIPMENT, PacketEntityEquipment.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_BLOCK_CHANGE, PacketBlockChange.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_MULTIBLOCK_CHANGE, PacketMultiBlockChange.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_RESPAWN, PacketRespawn.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_OPEN_SIGN_EDITOR, PacketOpenSignEditor.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_OBJECT, PacketSpawnObject.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_EXPERIENCE_ORB, PacketSpawnExperienceOrb.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_WEATHER_ENTITY, PacketSpawnWeatherEntity.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CHUNK_DATA, PacketChunkData.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_EFFECT, PacketEntityEffect.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_REMOVE_ENTITY_EFFECT, PacketRemoveEntityEffect.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_UPDATE_SIGN, PacketUpdateSignReceiving.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_ANIMATION, PacketEntityAnimation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_STATUS, PacketEntityStatus.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SOUND_EFFECT, PacketSoundEffect.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_PLAYER_POSITION_AND_LOOK, PacketPlayerPositionAndRotation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ATTACH_ENTITY, PacketAttachEntity.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_USE_BED, PacketUseBed.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_BLOCK_ENTITY_DATA, PacketBlockEntityMetadata.class);
    }

    int getProtocolVersion();

    int getPacketCommand(Packets.Serverbound p);

    String getName();

    Packets.Clientbound getPacketByCommand(ConnectionState s, int command);
}