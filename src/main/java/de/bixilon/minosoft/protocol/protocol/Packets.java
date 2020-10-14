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
import de.bixilon.minosoft.protocol.packets.clientbound.login.*;
import de.bixilon.minosoft.protocol.packets.clientbound.play.*;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusPong;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusResponse;

public class Packets {

    public enum Serverbound {
        HANDSHAKING_HANDSHAKE,
        STATUS_PING,
        STATUS_REQUEST,
        LOGIN_LOGIN_START,
        LOGIN_ENCRYPTION_RESPONSE,
        LOGIN_PLUGIN_RESPONSE,
        PLAY_TELEPORT_CONFIRM,
        PLAY_QUERY_BLOCK_NBT,
        PLAY_SET_DIFFICULTY,
        PLAY_CHAT_MESSAGE,
        PLAY_CLIENT_STATUS,
        PLAY_CLIENT_SETTINGS,
        PLAY_TAB_COMPLETE,
        PLAY_WINDOW_CONFIRMATION,
        PLAY_CLICK_WINDOW_BUTTON,
        PLAY_CLICK_WINDOW,
        PLAY_CLOSE_WINDOW,
        PLAY_PLUGIN_MESSAGE,
        PLAY_EDIT_BOOK,
        PLAY_ENTITY_NBT_REQUEST,
        PLAY_INTERACT_ENTITY,
        PLAY_KEEP_ALIVE,
        PLAY_LOCK_DIFFICULTY,
        PLAY_PLAYER_POSITION,
        PLAY_PLAYER_POSITION_AND_ROTATION,
        PLAY_PLAYER_ROTATION,
        PLAY_VEHICLE_MOVE,
        PLAY_STEER_BOAT,
        PLAY_PICK_ITEM,
        PLAY_CRAFT_RECIPE_REQUEST,
        PLAY_PLAYER_ABILITIES,
        PLAY_PLAYER_DIGGING,
        PLAY_ENTITY_ACTION,
        PLAY_STEER_VEHICLE,
        PLAY_RECIPE_BOOK_DATA,
        PLAY_NAME_ITEM,
        PLAY_RESOURCE_PACK_STATUS,
        PLAY_ADVANCEMENT_TAB,
        PLAY_SELECT_TRADE,
        PLAY_SET_BEACON_EFFECT,
        PLAY_HELD_ITEM_CHANGE,
        PLAY_UPDATE_COMMAND_BLOCK,
        PLAY_CREATIVE_INVENTORY_ACTION,
        PLAY_UPDATE_JIGSAW_BLOCK,
        PLAY_UPDATE_STRUCTURE_BLOCK,
        PLAY_UPDATE_SIGN,
        PLAY_ANIMATION,
        PLAY_SPECTATE,
        PLAY_PLAYER_BLOCK_PLACEMENT,
        PLAY_USE_ITEM,
        PLAY_UPDATE_COMMAND_BLOCK_MINECART,
        PLAY_GENERATE_STRUCTURE,
        PLAY_SET_DISPLAYED_RECIPE,
        PLAY_SET_RECIPE_BOOK_STATE,
        PLAY_PLAYER_GROUND_CHANGE,
        PLAY_PREPARE_CRAFTING_GRID,
        PLAY_VEHICLE_MOVEMENT,
        PLAY_QUERY_ENTITY_NBT;

        final ConnectionStates state;

        Serverbound() {
            this.state = ConnectionStates.valueOf(name().split("_")[0]);
        }

        public ConnectionStates getState() {
            return state;
        }
    }

    public enum Clientbound {
        STATUS_RESPONSE(PacketStatusResponse.class),
        STATUS_PONG(PacketStatusPong.class),
        LOGIN_DISCONNECT(PacketLoginDisconnect.class),
        LOGIN_ENCRYPTION_REQUEST(PacketEncryptionRequest.class),
        LOGIN_LOGIN_SUCCESS(PacketLoginSuccess.class),
        LOGIN_SET_COMPRESSION(PacketLoginSetCompression.class),
        LOGIN_PLUGIN_REQUEST(PacketLoginPluginRequest.class),
        PLAY_SPAWN_MOB(PacketSpawnMob.class),
        PLAY_SPAWN_EXPERIENCE_ORB(PacketSpawnExperienceOrb.class),
        PLAY_SPAWN_WEATHER_ENTITY(PacketSpawnWeatherEntity.class),
        PLAY_SPAWN_PAINTING(PacketSpawnPainting.class),
        PLAY_SPAWN_PLAYER(PacketSpawnPlayer.class),
        PLAY_ENTITY_ANIMATION(PacketEntityAnimation.class),
        PLAY_STATS_RESPONSE(PacketStatistics.class),
        PLAY_ACKNOWLEDGE_PLAYER_DIGGING(PacketAcknowledgePlayerDigging.class),
        PLAY_BLOCK_BREAK_ANIMATION(PacketBlockBreakAnimation.class),
        PLAY_BLOCK_ENTITY_DATA(PacketBlockEntityMetadata.class),
        PLAY_BLOCK_ACTION(PacketBlockAction.class),
        PLAY_BLOCK_CHANGE(PacketBlockChange.class),
        PLAY_BOSS_BAR(PacketBossBar.class),
        PLAY_SERVER_DIFFICULTY(PacketServerDifficulty.class),
        PLAY_CHAT_MESSAGE(PacketChatMessageReceiving.class),
        PLAY_MULTIBLOCK_CHANGE(PacketMultiBlockChange.class),
        PLAY_TAB_COMPLETE(PacketTabCompleteReceiving.class),
        PLAY_DECLARE_COMMANDS(null),
        PLAY_WINDOW_CONFIRMATION(PacketConfirmTransactionReceiving.class),
        PLAY_CLOSE_WINDOW(PacketCloseWindowReceiving.class),
        PLAY_WINDOW_ITEMS(PacketWindowItems.class),
        PLAY_WINDOW_PROPERTY(PacketWindowProperty.class),
        PLAY_SET_SLOT(PacketSetSlot.class),
        PLAY_SET_COOLDOWN(PacketSetCooldown.class),
        PLAY_PLUGIN_MESSAGE(PacketPluginMessageReceiving.class),
        PLAY_NAMED_SOUND_EFFECT(PacketNamedSoundEffect.class),
        PLAY_DISCONNECT(PacketDisconnect.class),
        PLAY_ENTITY_STATUS(PacketEntityStatus.class),
        PLAY_EXPLOSION(PacketExplosion.class),
        PLAY_UNLOAD_CHUNK(PacketUnloadChunk.class),
        PLAY_CHANGE_GAME_STATE(PacketChangeGameState.class),
        PLAY_OPEN_HORSE_WINDOW(PacketOpenHorseWindow.class),
        PLAY_KEEP_ALIVE(PacketKeepAlive.class),
        PLAY_CHUNK_DATA(PacketChunkData.class),
        PLAY_EFFECT(PacketEffect.class),
        PLAY_PARTICLE(PacketParticle.class),
        PLAY_UPDATE_LIGHT(PacketUpdateLight.class),
        PLAY_JOIN_GAME(PacketJoinGame.class),
        PLAY_MAP_DATA(PacketMapData.class),
        PLAY_TRADE_LIST(PacketTradeList.class),
        PLAY_ENTITY_MOVEMENT_AND_ROTATION(PacketEntityMovementAndRotation.class),
        PLAY_ENTITY_ROTATION(PacketEntityRotation.class),
        PLAY_ENTITY_MOVEMENT(PacketEntityMovement.class),
        PLAY_VEHICLE_MOVEMENT(PacketVehicleMovement.class),
        PLAY_OPEN_BOOK(PacketOpenBook.class),
        PLAY_OPEN_WINDOW(PacketOpenWindow.class),
        PLAY_OPEN_SIGN_EDITOR(PacketOpenSignEditor.class),
        PLAY_CRAFT_RECIPE_RESPONSE(PacketCraftRecipeResponse.class),
        PLAY_PLAYER_ABILITIES(PacketPlayerAbilitiesReceiving.class),
        PLAY_COMBAT_EVENT(PacketCombatEvent.class),
        PLAY_PLAYER_LIST_ITEM(PacketPlayerListItem.class),
        PLAY_FACE_PLAYER(PacketFacePlayer.class),
        PLAY_PLAYER_POSITION_AND_ROTATION(PacketPlayerPositionAndRotation.class),
        PLAY_UNLOCK_RECIPES(PacketUnlockRecipes.class),
        PLAY_DESTROY_ENTITIES(PacketDestroyEntity.class),
        PLAY_REMOVE_ENTITY_EFFECT(PacketRemoveEntityEffect.class),
        PLAY_RESOURCE_PACK_SEND(PacketResourcePackSend.class),
        PLAY_RESPAWN(PacketRespawn.class),
        PLAY_ENTITY_HEAD_ROTATION(PacketEntityHeadRotation.class),
        PLAY_SELECT_ADVANCEMENT_TAB(PacketSelectAdvancementTab.class),
        PLAY_WORLD_BORDER(PacketWorldBorder.class),
        PLAY_CAMERA(PacketCamera.class),
        PLAY_HELD_ITEM_CHANGE(PacketHeldItemChangeReceiving.class),
        PLAY_UPDATE_VIEW_POSITION(PacketUpdateViewPosition.class),
        PLAY_DISPLAY_SCOREBOARD(PacketScoreboardDisplayScoreboard.class),
        PLAY_ENTITY_METADATA(PacketEntityMetadata.class),
        PLAY_ATTACH_ENTITY(PacketAttachEntity.class),
        PLAY_ENTITY_VELOCITY(PacketEntityVelocity.class),
        PLAY_ENTITY_EQUIPMENT(PacketEntityEquipment.class),
        PLAY_SET_EXPERIENCE(PacketSetExperience.class),
        PLAY_UPDATE_HEALTH(PacketUpdateHealth.class),
        PLAY_SCOREBOARD_OBJECTIVE(PacketScoreboardObjective.class),
        PLAY_SET_PASSENGERS(PacketSetPassenger.class),
        PLAY_TEAMS(PacketTeams.class),
        PLAY_UPDATE_SCORE(PacketScoreboardUpdateScore.class),
        PLAY_SPAWN_POSITION(PacketSpawnLocation.class),
        PLAY_TIME_UPDATE(PacketTimeUpdate.class),
        PLAY_ENTITY_SOUND_EFFECT(PacketEntitySoundEffect.class),
        PLAY_SOUND_EFFECT(PacketSoundEffect.class),
        PLAY_STOP_SOUND(PacketStopSound.class),
        PLAY_PLAYER_LIST_HEADER_AND_FOOTER(PacketTabHeaderAndFooter.class),
        PLAY_NBT_QUERY_RESPONSE(PacketNBTQueryResponse.class),
        PLAY_COLLECT_ITEM(PacketCollectItem.class),
        PLAY_ENTITY_TELEPORT(PacketEntityTeleport.class),
        PLAY_ADVANCEMENTS(PacketAdvancements.class),
        PLAY_ENTITY_PROPERTIES(PacketEntityProperties.class),
        PLAY_ENTITY_EFFECT(PacketEntityEffect.class),
        PLAY_DECLARE_RECIPES(PacketDeclareRecipes.class),
        PLAY_TAGS(PacketTags.class),
        PLAY_USE_BED(PacketUseBed.class),
        PLAY_UPDATE_VIEW_DISTANCE(PacketUpdateViewDistance.class),
        PLAY_CHUNK_BULK(PacketChunkBulk.class),
        PLAY_UPDATE_SIGN(PacketUpdateSignReceiving.class),
        PLAY_STATISTICS(PacketStatistics.class),
        PLAY_SPAWN_ENTITY(PacketSpawnObject.class),
        PLAY_TITLE(PacketTitle.class),
        PLAY_ENTITY_INITIALISATION(PacketEntityInitialisation.class),
        PLAY_SET_COMPRESSION(PacketSetCompression.class),
        PLAY_ADVANCEMENT_PROGRESS(null);

        final ConnectionStates state;
        final Class<? extends ClientboundPacket> clazz;

        Clientbound(Class<? extends ClientboundPacket> clazz) {
            this.state = ConnectionStates.valueOf(name().split("_")[0]);
            this.clazz = clazz;
        }

        public ConnectionStates getState() {
            return state;
        }

        public Class<? extends ClientboundPacket> getClazz() {
            return clazz;
        }
    }
}
