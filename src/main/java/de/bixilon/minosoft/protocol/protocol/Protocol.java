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

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.clientbound.login.*;
import de.bixilon.minosoft.protocol.packets.clientbound.play.*;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusPong;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusResponse;

import java.util.HashMap;

public abstract class Protocol implements ProtocolInterface {
    static final HashBiMap<Packets.Clientbound, Class<? extends ClientboundPacket>> packetClassMapping = HashBiMap.create();

    static {
        packetClassMapping.put(Packets.Clientbound.STATUS_RESPONSE, PacketStatusResponse.class);
        packetClassMapping.put(Packets.Clientbound.STATUS_PONG, PacketStatusPong.class);
        packetClassMapping.put(Packets.Clientbound.LOGIN_ENCRYPTION_REQUEST, PacketEncryptionRequest.class);
        packetClassMapping.put(Packets.Clientbound.LOGIN_LOGIN_SUCCESS, PacketLoginSuccess.class);
        packetClassMapping.put(Packets.Clientbound.LOGIN_DISCONNECT, PacketLoginDisconnect.class);
        packetClassMapping.put(Packets.Clientbound.LOGIN_SET_COMPRESSION, PacketLoginSetCompression.class);
        packetClassMapping.put(Packets.Clientbound.LOGIN_PLUGIN_REQUEST, PacketLoginPluginRequest.class);

        packetClassMapping.put(Packets.Clientbound.PLAY_JOIN_GAME, PacketJoinGame.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_PLAYER_INFO, PacketPlayerInfo.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_TIME_UPDATE, PacketTimeUpdate.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_KEEP_ALIVE, PacketKeepAlive.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CHUNK_BULK, PacketChunkBulk.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_UPDATE_HEALTH, PacketUpdateHealth.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_PLUGIN_MESSAGE, PacketPluginMessageReceiving.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_POSITION, PacketSpawnLocation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CHAT_MESSAGE, PacketChatMessageReceiving.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_DISCONNECT, PacketDisconnect.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_HELD_ITEM_CHANGE, PacketHeldItemChangeReceiving.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SET_EXPERIENCE, PacketSetExperience.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CHANGE_GAME_STATE, PacketChangeGameState.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_MOB, PacketSpawnMob.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_MOVEMENT_AND_ROTATION, PacketEntityMovementAndRotation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_MOVEMENT, PacketEntityMovement.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_ROTATION, PacketEntityRotation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_DESTROY_ENTITIES, PacketDestroyEntity.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_VELOCITY, PacketEntityVelocity.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_PLAYER, PacketSpawnPlayer.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_TELEPORT, PacketEntityTeleport.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_HEAD_ROTATION, PacketEntityHeadRotation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_WINDOW_ITEMS, PacketWindowItems.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_METADATA, PacketEntityMetadata.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_EQUIPMENT, PacketEntityEquipment.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_BLOCK_CHANGE, PacketBlockChange.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_MULTIBLOCK_CHANGE, PacketMultiBlockChange.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_RESPAWN, PacketRespawn.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_OPEN_SIGN_EDITOR, PacketOpenSignEditor.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_ENTITY, PacketSpawnObject.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_EXPERIENCE_ORB, PacketSpawnExperienceOrb.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_WEATHER_ENTITY, PacketSpawnWeatherEntity.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CHUNK_DATA, PacketChunkData.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_EFFECT, PacketEntityEffect.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_REMOVE_ENTITY_EFFECT, PacketRemoveEntityEffect.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_UPDATE_SIGN, PacketUpdateSignReceiving.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_ANIMATION, PacketEntityAnimation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_STATUS, PacketEntityStatus.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_NAMED_SOUND_EFFECT, PacketNamedSoundEffect.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_PLAYER_POSITION_AND_ROTATION, PacketPlayerPositionAndRotation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ATTACH_ENTITY, PacketAttachEntity.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_USE_BED, PacketUseBed.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_BLOCK_ENTITY_DATA, PacketBlockEntityMetadata.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_BLOCK_BREAK_ANIMATION, PacketBlockBreakAnimation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_BLOCK_ACTION, PacketBlockAction.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_EXPLOSION, PacketExplosion.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_COLLECT_ITEM, PacketCollectItem.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_OPEN_WINDOW, PacketOpenWindow.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CLOSE_WINDOW, PacketCloseWindowReceiving.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SET_SLOT, PacketSetSlot.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_WINDOW_CONFIRMATION, PacketConfirmTransactionReceiving.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_PLAYER_ABILITIES, PacketPlayerAbilitiesReceiving.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_STATISTICS, PacketStatistics.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_PAINTING, PacketSpawnPainting.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_PARTICLE, PacketParticle.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_EFFECT, PacketEffect.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SCOREBOARD_OBJECTIVE, PacketScoreboardObjective.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_UPDATE_SCORE, PacketScoreboardUpdateScore.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_TEAMS, PacketTeams.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_DISPLAY_SCOREBOARD, PacketScoreboardDisplayScoreboard.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_MAP_DATA, PacketMapData.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SERVER_DIFFICULTY, PacketServerDifficulty.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_LIST_HEADER_AND_FOOTER, PacketTabHeaderAndFooter.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_RESOURCE_PACK_SEND, PackerResourcePackSend.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_PROPERTIES, PacketEntityProperties.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_WORLD_BORDER, PacketWorldBorder.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_TITLE, PacketTitle.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_COMBAT_EVENT, PacketCombatEvent.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CAMERA, PacketCamera.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_UNLOAD_CHUNK, PacketUnloadChunk.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SOUND_EFFECT, PacketSoundEffect.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_BOSS_BAR, PacketBossBar.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SET_PASSENGERS, PacketSetPassenger.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CRAFT_RECIPE_RESPONSE, PacketCraftRecipeResponse.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_UNLOCK_RECIPES, PacketUnlockRecipes.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SELECT_ADVANCEMENT_TAB, PacketSelectAdvancementTab.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ADVANCEMENTS, PacketAdvancements.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_NBT_QUERY_RESPONSE, PacketNBTQueryResponse.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_FACE_PLAYER, PacketFacePlayer.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_TAGS, PacketTags.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_DECLARE_RECIPES, PacketDeclareRecipes.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_STOP_SOUND, PacketStopSound.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_UPDATE_LIGHT, PacketUpdateLight.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_UPDATE_VIEW_DISTANCE, PacketUpdateViewDistance.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_UPDATE_VIEW_POSITION, PacketUpdateViewPosition.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_OPEN_HORSE_WINDOW, PacketOpenHorseWindow.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_TRADE_LIST, PacketTradeList.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_OPEN_BOOK, PacketOpenBook.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ACKNOWLEDGE_PLAYER_DIGGING, PacketAcknowledgePlayerDigging.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_ENTITY_SOUND_EFFECT, PacketEntitySoundEffect.class);
    }

    protected final HashMap<ConnectionState, HashBiMap<Packets.Serverbound, Integer>> serverboundPacketMapping;
    protected final HashMap<ConnectionState, HashBiMap<Packets.Clientbound, Integer>> clientboundPacketMapping;

    public Protocol() {
        serverboundPacketMapping = new HashMap<>();
        serverboundPacketMapping.put(ConnectionState.HANDSHAKING, HashBiMap.create());
        serverboundPacketMapping.put(ConnectionState.STATUS, HashBiMap.create());
        serverboundPacketMapping.put(ConnectionState.LOGIN, HashBiMap.create());
        serverboundPacketMapping.put(ConnectionState.PLAY, HashBiMap.create());

        registerPacket(Packets.Serverbound.HANDSHAKING_HANDSHAKE, 0x00);
        // status
        registerPacket(Packets.Serverbound.STATUS_REQUEST, 0x00);
        registerPacket(Packets.Serverbound.STATUS_PING, 0x01);
        // login
        registerPacket(Packets.Serverbound.LOGIN_LOGIN_START, 0x00);
        registerPacket(Packets.Serverbound.LOGIN_ENCRYPTION_RESPONSE, 0x01);


        clientboundPacketMapping = new HashMap<>();
        clientboundPacketMapping.put(ConnectionState.HANDSHAKING, HashBiMap.create());
        clientboundPacketMapping.put(ConnectionState.STATUS, HashBiMap.create());
        clientboundPacketMapping.put(ConnectionState.LOGIN, HashBiMap.create());
        clientboundPacketMapping.put(ConnectionState.PLAY, HashBiMap.create());

        registerPacket(Packets.Clientbound.STATUS_RESPONSE, 0x00);
        registerPacket(Packets.Clientbound.STATUS_PONG, 0x01);
        // login
        registerPacket(Packets.Clientbound.LOGIN_DISCONNECT, 0x00);
        registerPacket(Packets.Clientbound.LOGIN_ENCRYPTION_REQUEST, 0x01);
        registerPacket(Packets.Clientbound.LOGIN_LOGIN_SUCCESS, 0x02);
    }

    public static Class<? extends ClientboundPacket> getPacketByPacket(Packets.Clientbound p) {
        return packetClassMapping.get(p);
    }

    public static ProtocolVersion getLowestVersionSupported() {
        return ProtocolVersion.VERSION_1_7_10;
    }

    protected void registerPacket(Packets.Serverbound packet, int id) {
        if (serverboundPacketMapping.get(packet.getState()).containsKey(packet)) {
            throw new IllegalArgumentException(String.format("%s is already registered!", packet));
        }
        if (serverboundPacketMapping.get(packet.getState()).containsValue(id)) {
            throw new IllegalArgumentException(String.format("%x is already registered!", id));
        }
        serverboundPacketMapping.get(packet.getState()).put(packet, id);
    }

    protected void registerPacket(Packets.Clientbound packet, int id) {
        if (clientboundPacketMapping.get(packet.getState()).containsKey(packet)) {
            throw new IllegalArgumentException(String.format("%s is already registered!", packet));
        }
        if (clientboundPacketMapping.get(packet.getState()).containsValue(id)) {
            throw new IllegalArgumentException(String.format("%x is already registered!", id));
        }
        clientboundPacketMapping.get(packet.getState()).put(packet, id);
    }

    public int getPacketCommand(Packets.Serverbound p) {
        return serverboundPacketMapping.get(p.getState()).get(p);
    }

    public Packets.Clientbound getPacketByCommand(ConnectionState state, int command) {
        return clientboundPacketMapping.get(state).inverse().get(command);
    }

    @Override
    public int hashCode() {
        return getProtocolVersionNumber();
    }
}