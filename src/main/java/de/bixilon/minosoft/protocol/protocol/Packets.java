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

        private final ConnectionStates state;

        Serverbound() {
            this.state = ConnectionStates.valueOf(name().split("_")[0]);
        }

        public ConnectionStates getState() {
            return this.state;
        }
    }

    public enum Clientbound {
        STATUS_RESPONSE(PacketStatusResponse::new),
        STATUS_PONG(PacketStatusPong::new),
        LOGIN_DISCONNECT(PacketLoginDisconnect::new),
        LOGIN_ENCRYPTION_REQUEST(PacketEncryptionRequest::new),
        LOGIN_LOGIN_SUCCESS(PacketLoginSuccess::new),
        LOGIN_SET_COMPRESSION(PacketLoginSetCompression::new),
        LOGIN_PLUGIN_REQUEST(PacketLoginPluginRequest::new),
        PLAY_SPAWN_MOB(PacketSpawnMob::new),
        PLAY_SPAWN_EXPERIENCE_ORB(PacketSpawnExperienceOrb::new),
        PLAY_SPAWN_WEATHER_ENTITY(PacketSpawnWeatherEntity::new),
        PLAY_SPAWN_PAINTING(PacketSpawnPainting::new),
        PLAY_SPAWN_PLAYER(PacketSpawnPlayer::new),
        PLAY_ENTITY_ANIMATION(PacketEntityAnimation::new),
        PLAY_STATS_RESPONSE(PacketStatistics::new),
        PLAY_ACKNOWLEDGE_PLAYER_DIGGING(PacketAcknowledgePlayerDigging::new),
        PLAY_BLOCK_BREAK_ANIMATION(PacketBlockBreakAnimation::new),
        PLAY_BLOCK_ENTITY_DATA(PacketBlockEntityMetadata::new),
        PLAY_BLOCK_ACTION(PacketBlockAction::new),
        PLAY_BLOCK_CHANGE(PacketBlockChange::new),
        PLAY_BOSS_BAR(PacketBossBar::new),
        PLAY_SERVER_DIFFICULTY(PacketServerDifficulty::new),
        PLAY_CHAT_MESSAGE(PacketChatMessageReceiving::new),
        PLAY_MULTIBLOCK_CHANGE(PacketMultiBlockChange::new),
        PLAY_TAB_COMPLETE(PacketTabCompleteReceiving::new),
        PLAY_DECLARE_COMMANDS(PacketDeclareCommands::new),
        PLAY_WINDOW_CONFIRMATION(PacketConfirmTransactionReceiving::new),
        PLAY_CLOSE_WINDOW(PacketCloseWindowReceiving::new),
        PLAY_WINDOW_ITEMS(PacketWindowItems::new),
        PLAY_WINDOW_PROPERTY(PacketWindowProperty::new),
        PLAY_SET_SLOT(PacketSetSlot::new),
        PLAY_SET_COOLDOWN(PacketSetCooldown::new),
        PLAY_PLUGIN_MESSAGE(PacketPluginMessageReceiving::new),
        PLAY_NAMED_SOUND_EFFECT(PacketNamedSoundEffect::new),
        PLAY_DISCONNECT(PacketDisconnect::new),
        PLAY_ENTITY_EVENT(PacketEntityEvent::new),
        PLAY_EXPLOSION(PacketExplosion::new),
        PLAY_UNLOAD_CHUNK(PacketUnloadChunk::new),
        PLAY_CHANGE_GAME_STATE(PacketChangeGameState::new),
        PLAY_OPEN_HORSE_WINDOW(PacketOpenHorseWindow::new),
        PLAY_KEEP_ALIVE(PacketKeepAlive::new),
        PLAY_CHUNK_DATA(PacketChunkData::new),
        PLAY_EFFECT(PacketEffect::new),
        PLAY_PARTICLE(PacketParticle::new),
        PLAY_UPDATE_LIGHT(PacketUpdateLight::new),
        PLAY_JOIN_GAME(PacketJoinGame::new),
        PLAY_MAP_DATA(PacketMapData::new),
        PLAY_TRADE_LIST(PacketTradeList::new),
        PLAY_ENTITY_MOVEMENT_AND_ROTATION(PacketEntityMovementAndRotation::new),
        PLAY_ENTITY_ROTATION(PacketEntityRotation::new),
        PLAY_ENTITY_MOVEMENT(PacketEntityMovement::new),
        PLAY_VEHICLE_MOVEMENT(PacketVehicleMovement::new),
        PLAY_OPEN_BOOK(PacketOpenBook::new),
        PLAY_OPEN_WINDOW(PacketOpenWindow::new),
        PLAY_OPEN_SIGN_EDITOR(PacketOpenSignEditor::new),
        PLAY_CRAFT_RECIPE_RESPONSE(PacketCraftRecipeResponse::new),
        PLAY_PLAYER_ABILITIES(PacketPlayerAbilitiesReceiving::new),
        PLAY_COMBAT_EVENT(PacketCombatEvent::new),
        PLAY_PLAYER_LIST_ITEM(PacketPlayerListItem::new),
        PLAY_FACE_PLAYER(PacketFacePlayer::new),
        PLAY_PLAYER_POSITION_AND_ROTATION(PacketPlayerPositionAndRotation::new),
        PLAY_UNLOCK_RECIPES(PacketUnlockRecipes::new),
        PLAY_DESTROY_ENTITIES(PacketDestroyEntity::new),
        PLAY_REMOVE_ENTITY_EFFECT(PacketRemoveEntityEffect::new),
        PLAY_RESOURCE_PACK_SEND(PacketResourcePackSend::new),
        PLAY_RESPAWN(PacketRespawn::new),
        PLAY_ENTITY_HEAD_ROTATION(PacketEntityHeadRotation::new),
        PLAY_SELECT_ADVANCEMENT_TAB(PacketSelectAdvancementTab::new),
        PLAY_WORLD_BORDER(PacketWorldBorder::new),
        PLAY_CAMERA(PacketCamera::new),
        PLAY_HELD_ITEM_CHANGE(PacketHeldItemChangeReceiving::new),
        PLAY_UPDATE_VIEW_POSITION(PacketUpdateViewPosition::new),
        PLAY_DISPLAY_SCOREBOARD(PacketScoreboardDisplayScoreboard::new),
        PLAY_ENTITY_METADATA(PacketEntityMetadata::new),
        PLAY_ATTACH_ENTITY(PacketAttachEntity::new),
        PLAY_ENTITY_VELOCITY(PacketEntityVelocity::new),
        PLAY_ENTITY_EQUIPMENT(PacketEntityEquipment::new),
        PLAY_SET_EXPERIENCE(PacketSetExperience::new),
        PLAY_UPDATE_HEALTH(PacketUpdateHealth::new),
        PLAY_SCOREBOARD_OBJECTIVE(PacketScoreboardObjective::new),
        PLAY_SET_PASSENGERS(PacketSetPassenger::new),
        PLAY_TEAMS(PacketTeams::new),
        PLAY_UPDATE_SCORE(PacketScoreboardUpdateScore::new),
        PLAY_SPAWN_POSITION(PacketSpawnPosition::new),
        PLAY_TIME_UPDATE(PacketTimeUpdate::new),
        PLAY_ENTITY_SOUND_EFFECT(PacketEntitySoundEffect::new),
        PLAY_SOUND_EFFECT(PacketSoundEffect::new),
        PLAY_STOP_SOUND(PacketStopSound::new),
        PLAY_PLAYER_LIST_HEADER_AND_FOOTER(PacketTabHeaderAndFooter::new),
        PLAY_NBT_QUERY_RESPONSE(PacketNBTQueryResponse::new),
        PLAY_COLLECT_ITEM(PacketCollectItem::new),
        PLAY_ENTITY_TELEPORT(PacketEntityTeleport::new),
        PLAY_ADVANCEMENTS(PacketAdvancements::new),
        PLAY_ENTITY_PROPERTIES(PacketEntityProperties::new),
        PLAY_ENTITY_EFFECT(PacketEntityEffect::new),
        PLAY_DECLARE_RECIPES(PacketDeclareRecipes::new),
        PLAY_TAGS(PacketTags::new),
        PLAY_USE_BED(PacketUseBed::new),
        PLAY_UPDATE_VIEW_DISTANCE(PacketUpdateViewDistance::new),
        PLAY_CHUNK_BULK(PacketChunkBulk::new),
        PLAY_UPDATE_SIGN(PacketUpdateSignReceiving::new),
        PLAY_STATISTICS(PacketStatistics::new),
        PLAY_SPAWN_ENTITY(PacketSpawnObject::new),
        PLAY_TITLE(PacketTitle::new),
        PLAY_ENTITY_INITIALISATION(PacketEntityInitialisation::new),
        PLAY_SET_COMPRESSION(PacketSetCompression::new),
        PLAY_ADVANCEMENT_PROGRESS(null),
        PLAY_SCULK_VIBRATION_SIGNAL(PacketSculkVibrationSignal::new);

        private final ConnectionStates state;
        private final PacketInstanceCreator creator;

        Clientbound(PacketInstanceCreator creator) {
            this.state = ConnectionStates.valueOf(name().split("_")[0]);
            this.creator = creator;
        }

        public ConnectionStates getState() {
            return this.state;
        }

        public ClientboundPacket createNewInstance() {
            return this.creator.createNewInstance();
        }
    }

    private interface PacketInstanceCreator {
        ClientboundPacket createNewInstance();
    }
}
