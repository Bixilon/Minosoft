/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License,
 or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not,
 see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB,
 the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.protocol

import de.bixilon.minosoft.protocol.ErrorHandler
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket
import de.bixilon.minosoft.protocol.packets.clientbound.StatusClientboundPacket
import de.bixilon.minosoft.protocol.packets.clientbound.login.*
import de.bixilon.minosoft.protocol.packets.clientbound.play.*
import de.bixilon.minosoft.protocol.packets.clientbound.play.combat.CombatEventPacketFactory
import de.bixilon.minosoft.protocol.packets.clientbound.play.title.TitlePacketFactory
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusPong
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusResponse
import de.bixilon.minosoft.protocol.packets.serverbound.ServerboundPacket
import de.bixilon.minosoft.protocol.packets.serverbound.handshaking.HandshakeServerboundPacket
import de.bixilon.minosoft.protocol.packets.serverbound.login.EncryptionResponseServerboundPacket
import de.bixilon.minosoft.protocol.packets.serverbound.login.LoginPluginResponseServerboundPacket
import de.bixilon.minosoft.protocol.packets.serverbound.login.LoginStartServerboundPacket
import de.bixilon.minosoft.protocol.packets.serverbound.play.*
import de.bixilon.minosoft.protocol.packets.serverbound.play.interact.BaseInteractEntityServerboundPacket
import de.bixilon.minosoft.protocol.packets.serverbound.status.StatusPingServerboundPacket
import de.bixilon.minosoft.protocol.packets.serverbound.status.StatusRequestServerboundPacket

class PacketTypes {
    enum class Serverbound(val clazz: Class<out ServerboundPacket>? = null) {
        HANDSHAKING_HANDSHAKE(HandshakeServerboundPacket::class.java),
        STATUS_PING(StatusPingServerboundPacket::class.java),
        STATUS_REQUEST(StatusRequestServerboundPacket::class.java),
        LOGIN_LOGIN_START(LoginStartServerboundPacket::class.java),
        LOGIN_ENCRYPTION_RESPONSE(EncryptionResponseServerboundPacket::class.java),
        LOGIN_PLUGIN_RESPONSE(LoginPluginResponseServerboundPacket::class.java),
        PLAY_TELEPORT_CONFIRMATION(TeleportConfirmServerboundPacket::class.java),
        PLAY_QUERY_BLOCK_NBT,
        PLAY_SET_DIFFICULTY,
        PLAY_CHAT_MESSAGE(ChatMessageServerboundPacket::class.java),
        PLAY_CLIENT_ACTION(ClientActionServerboundPacket::class.java),
        PLAY_CLIENT_SETTINGS(ClientSettingsServerboundPacket::class.java),
        PLAY_TAB_COMPLETE(TabCompleteServerboundPacket::class.java),
        PLAY_WINDOW_CONFIRMATION(WindowConfirmationServerboundPacket::class.java),
        PLAY_CLICK_WINDOW_ACTION(ClickWindowActionServerboundPacket::class.java),
        PLAY_CLICK_WINDOW_SLOT(ClickWindowSlotServerboundPacket::class.java),
        PLAY_CLOSE_WINDOW(CloseWindowServerboundPacket::class.java),
        PLAY_PLUGIN_MESSAGE(PluginMessageServerboundPacket::class.java),
        PLAY_EDIT_BOOK,
        PLAY_ENTITY_NBT_REQUEST(EntityNBTRequestServerboundPacket::class.java),
        PLAY_INTERACT_ENTITY(BaseInteractEntityServerboundPacket::class.java),
        PLAY_KEEP_ALIVE(KeepAliveServerboundPacket::class.java),
        PLAY_LOCK_DIFFICULTY,
        PLAY_PLAYER_POSITION(PlayerPositionServerboundPacket::class.java),
        PLAY_PLAYER_POSITION_AND_ROTATION(PlayerPositionAndRotationServerboundPacket::class.java),
        PLAY_PLAYER_ROTATION(PlayerRotationServerboundPacket::class.java),
        PLAY_VEHICLE_MOVE(PacketVehicleMovementServerboundPacket::class.java),
        PLAY_STEER_BOAT(SteerBoatServerboundPacket::class.java),
        PLAY_PICK_ITEM,
        PLAY_CRAFTING_RECIPE_REQUEST(CraftingRecipeRequestServerboundPacket::class.java),
        PLAY_PLAYER_ABILITIES(PlayerAbilitiesServerboundPacket::class.java),
        PLAY_PLAYER_DIGGING(PlayerDiggingServerboundPacket::class.java),
        PLAY_ENTITY_ACTION(EntityActionServerboundPacket::class.java),
        PLAY_STEER_VEHICLE(SteerVehicleServerboundPacket::class.java),
        PLAY_RECIPE_BOOK_STATE(RecipeBookStateServerboundPacket::class.java),
        PLAY_NAME_ITEM(NameItemServerboundPacket::class.java),
        PLAY_RESOURCE_PACK_STATUS(ResourcePackStatusServerboundPacket::class.java),
        PLAY_ADVANCEMENT_TAB(AdvancementTabServerboundPacket::class.java),
        PLAY_SELECT_TRADE(SelectTradeServerboundPacket::class.java),
        PLAY_SET_BEACON_EFFECT(SetBeaconEffectServerboundPacket::class.java),
        PLAY_HELD_ITEM_CHANGE(HeldItemChangeServerboundPacket::class.java),
        PLAY_UPDATE_COMMAND_BLOCK(UpdateCommandBlockServerboundPacket::class.java),
        PLAY_CREATIVE_INVENTORY_ACTION(CreativeInventoryActionServerboundPacket::class.java),
        PLAY_UPDATE_JIGSAW_BLOCK(UpdateJigsawBlockServerboundPacket::class.java),
        PLAY_UPDATE_STRUCTURE_BLOCK(UpdateStructureBlockServerboundPacket::class.java),
        PLAY_UPDATE_SIGN(UpdateSignServerboundPacket::class.java),
        PLAY_HAND_ANIMATION(HandAnimationServerboundPacket::class.java),
        PLAY_SPECTATE_ENTITY(SpectateEntityServerboundPacket::class.java),
        PLAY_PLACE_BLOCK(PlaceBlockServerboundPacket::class.java),
        PLAY_USE_ITEM(UseItemServerboundPacket::class.java),
        PLAY_UPDATE_COMMAND_BLOCK_MINECART(UpdateCommandBlockMinecartServerboundPacket::class.java),
        PLAY_GENERATE_STRUCTURE(GenerateStructureServerboundPacket::class.java),
        PLAY_SET_DISPLAYED_RECIPE(SetDisplayedRecipeServerboundPacket::class.java),
        PLAY_PLAYER_GROUND_CHANGE,
        PLAY_PREPARE_CRAFTING_GRID,
        PLAY_VEHICLE_MOVEMENT,
        PLAY_QUERY_ENTITY_NBT,
        ;

        val state: ConnectionStates = ConnectionStates.valueOf(name.split("_".toRegex()).toTypedArray()[0])

        companion object {
            private val MAPPING: Map<Class<out ServerboundPacket>, Serverbound>

            init {
                val mapping: MutableMap<Class<out ServerboundPacket>, Serverbound> = mutableMapOf()

                for (value in values()) {
                    if (value.clazz == null) {
                        continue
                    }
                    mapping[value.clazz] = value
                }

                MAPPING = mapping.toMap()
            }

            fun getPacketType(`class`: Class<out ServerboundPacket>): Serverbound {
                var checkedClass: Class<*> = `class`

                while (checkedClass != ServerboundPacket::class.java) {
                    MAPPING[checkedClass]?.let {
                        return it
                    }
                    checkedClass = checkedClass.superclass
                }
                error("Can not find packet type for class $`class`")
            }
        }
    }


    enum class Clientbound(
        val playFactory: ((buffer: PlayInByteBuffer) -> PlayClientboundPacket)? = null,
        val statusFactory: ((buffer: InByteBuffer) -> StatusClientboundPacket)? = null,
        val isThreadSafe: Boolean = true,
        val errorHandler: ErrorHandler? = null,
    ) {
        STATUS_RESPONSE(statusFactory = { PacketStatusResponse(it) }, isThreadSafe = false),
        STATUS_PONG(statusFactory = { PacketStatusPong(it) }, isThreadSafe = false),
        LOGIN_DISCONNECT({ PacketLoginDisconnect(it) }, isThreadSafe = false),
        LOGIN_ENCRYPTION_REQUEST({ PacketEncryptionRequest(it) }, isThreadSafe = false),
        LOGIN_LOGIN_SUCCESS({ PacketLoginSuccess(it) }, isThreadSafe = false),
        LOGIN_SET_COMPRESSION({ PacketLoginSetCompression(it) }, isThreadSafe = false),
        LOGIN_PLUGIN_REQUEST({ PacketLoginPluginRequest(it) }),
        PLAY_SPAWN_MOB({ PacketSpawnMob(it) }, isThreadSafe = false),
        PLAY_SPAWN_EXPERIENCE_ORB({ PacketSpawnExperienceOrb(it) }, isThreadSafe = false),
        PLAY_SPAWN_WEATHER_ENTITY({ PacketSpawnWeatherEntity(it) }, isThreadSafe = false),
        PLAY_SPAWN_PAINTING({ PacketSpawnPainting(it) }, isThreadSafe = false),
        PLAY_SPAWN_PLAYER({ PacketSpawnPlayer(it) }, isThreadSafe = false),
        PLAY_ENTITY_ANIMATION({ PacketEntityAnimation(it) }),
        PLAY_STATS_RESPONSE({ PacketStatistics(it) }),
        PLAY_ACKNOWLEDGE_PLAYER_DIGGING({ PacketAcknowledgePlayerDigging(it) }),
        PLAY_BLOCK_BREAK_ANIMATION({ PacketBlockBreakAnimation(it) }),
        PLAY_BLOCK_ENTITY_DATA({ PacketBlockEntityMetadata(it) }),
        PLAY_BLOCK_ACTION({ PacketBlockAction(it) }),
        PLAY_BLOCK_CHANGE({ PacketBlockChange(it) }),
        PLAY_BOSS_BAR({ PacketBossBar(it) }),
        PLAY_SERVER_DIFFICULTY({ PacketReceiveDifficulty(it) }),
        PLAY_CHAT_MESSAGE({ PacketChatMessageReceiving(it) }),
        PLAY_MULTIBLOCK_CHANGE({ PacketMultiBlockChange(it) }),
        PLAY_TAB_COMPLETE({ PacketTabCompleteReceiving(it) }),
        PLAY_DECLARE_COMMANDS({ PacketDeclareCommands(it) }),
        PLAY_WINDOW_CONFIRMATION({ PacketConfirmTransactionReceiving(it) }),
        PLAY_CLOSE_WINDOW({ PacketCloseWindowReceiving(it) }),
        PLAY_WINDOW_ITEMS({ PacketWindowItems(it) }),
        PLAY_WINDOW_PROPERTY({ PacketWindowProperty(it) }),
        PLAY_SET_SLOT({ PacketSetSlot(it) }),
        PLAY_SET_COOLDOWN({ PacketSetCooldown(it) }),
        PLAY_PLUGIN_MESSAGE({ PacketPluginMessageReceiving(it) }),
        PLAY_NAMED_SOUND_EFFECT({ PacketNamedSoundEffect(it) }),
        PLAY_DISCONNECT({ PacketDisconnect(it) }, isThreadSafe = false),
        PLAY_ENTITY_EVENT({ PacketEntityEvent(it) }),
        PLAY_EXPLOSION({ PacketExplosion(it) }),
        PLAY_UNLOAD_CHUNK({ PacketUnloadChunk(it) }),
        PLAY_CHANGE_GAME_STATE({ PacketChangeGameState(it) }),
        PLAY_OPEN_HORSE_WINDOW({ PacketOpenHorseWindow(it) }),
        PLAY_KEEP_ALIVE({ PacketKeepAlive(it) }),
        PLAY_CHUNK_DATA({ PacketChunkData(it) }),
        PLAY_EFFECT({ PacketEffect(it) }),
        PLAY_PARTICLE({ PacketParticle(it) }),
        PLAY_UPDATE_LIGHT({ PacketUpdateLight(it) }),
        PLAY_JOIN_GAME({ PacketJoinGame(it) }, isThreadSafe = false, errorHandler = PacketJoinGame),
        PLAY_MAP_DATA({ PacketMapData(it) }),
        PLAY_TRADE_LIST({ PacketTradeList(it) }),
        PLAY_ENTITY_MOVEMENT_AND_ROTATION({ PacketEntityMovementAndRotation(it) }),
        PLAY_ENTITY_ROTATION({ PacketEntityRotation(it) }),
        PLAY_ENTITY_MOVEMENT({ PacketEntityMovement(it) }),
        PLAY_VEHICLE_MOVEMENT({ PacketVehicleMovement(it) }),
        PLAY_OPEN_BOOK({ PacketOpenBook(it) }),
        PLAY_OPEN_WINDOW({ PacketOpenWindow(it) }),
        PLAY_OPEN_SIGN_EDITOR({ PacketOpenSignEditor(it) }),
        PLAY_CRAFT_RECIPE_RESPONSE({ PacketCraftRecipeResponse(it) }),
        PLAY_PLAYER_ABILITIES({ PacketPlayerAbilitiesReceiving(it) }),
        PLAY_COMBAT_EVENT({ CombatEventPacketFactory.createPacket(it) }),
        PLAY_PLAYER_LIST_ITEM({ PacketPlayerListItem(it) }),
        PLAY_FACE_PLAYER({ PacketFacePlayer(it) }),
        PLAY_PLAYER_POSITION_AND_ROTATION({ PacketPlayerPositionAndRotation(it) }),
        PLAY_UNLOCK_RECIPES({ PacketUnlockRecipes(it) }),
        PLAY_DESTROY_ENTITIES({ PacketDestroyEntity(it) }),
        PLAY_REMOVE_ENTITY_EFFECT({ PacketRemoveEntityStatusEffect(it) }),
        PLAY_RESOURCE_PACK_SEND({ PacketResourcePackSend(it) }),
        PLAY_RESPAWN({ PacketRespawn(it) }, isThreadSafe = false),
        PLAY_ENTITY_HEAD_ROTATION({ PacketEntityHeadRotation(it) }),
        PLAY_SELECT_ADVANCEMENT_TAB({ PacketSelectAdvancementTab(it) }),
        PLAY_WORLD_BORDER({ PacketWorldBorder(it) }),
        PLAY_CAMERA({ PacketCamera(it) }),
        PLAY_HELD_ITEM_CHANGE({ PacketHeldItemChangeReceiving(it) }),
        PLAY_UPDATE_VIEW_POSITION({ PacketUpdateViewPosition(it) }),
        PLAY_DISPLAY_SCOREBOARD({ PacketScoreboardDisplayScoreboard(it) }),
        PLAY_ENTITY_METADATA({ PacketEntityMetadata(it) }),
        PLAY_ATTACH_ENTITY({ PacketAttachEntity(it) }),
        PLAY_ENTITY_VELOCITY({ PacketEntityVelocity(it) }),
        PLAY_ENTITY_EQUIPMENT({ PacketEntityEquipment(it) }),
        PLAY_SET_EXPERIENCE({ PacketSetExperience(it) }),
        PLAY_UPDATE_HEALTH({ PacketUpdateHealth(it) }),
        PLAY_SCOREBOARD_OBJECTIVE({ PacketScoreboardObjective(it) }),
        PLAY_SET_PASSENGERS({ PacketSetPassenger(it) }),
        PLAY_TEAMS({ PacketTeams(it) }),
        PLAY_UPDATE_SCORE({ PacketScoreboardUpdateScore(it) }),
        PLAY_SPAWN_POSITION({ PacketSpawnPosition(it) }),
        PLAY_TIME_UPDATE({ PacketTimeUpdate(it) }),
        PLAY_ENTITY_SOUND_EFFECT({ PacketEntitySoundEffect(it) }),
        PLAY_SOUND_EFFECT({ PacketSoundEffect(it) }),
        PLAY_STOP_SOUND({ PacketStopSound(it) }),
        PLAY_PLAYER_LIST_HEADER_AND_FOOTER({ PacketTabHeaderAndFooter(it) }),
        PLAY_NBT_QUERY_RESPONSE({ PacketNBTQueryResponse(it) }),
        PLAY_COLLECT_ITEM({ PacketCollectItem(it) }),
        PLAY_ENTITY_TELEPORT({ PacketEntityTeleport(it) }, isThreadSafe = false),
        PLAY_ADVANCEMENTS({ PacketAdvancements(it) }),
        PLAY_ENTITY_PROPERTIES({ PacketEntityProperties(it) }),
        PLAY_ENTITY_EFFECT({ PacketEntityEffect(it) }),
        PLAY_DECLARE_RECIPES({ PacketDeclareRecipes(it) }),
        PLAY_TAGS({ PacketTags(it) }),
        PLAY_USE_BED({ PacketUseBed(it) }),
        PLAY_UPDATE_VIEW_DISTANCE({ PacketUpdateViewDistance(it) }),
        PLAY_CHUNK_BULK({ PacketChunkBulk(it) }),
        PLAY_UPDATE_SIGN({ PacketUpdateSignReceiving(it) }),
        PLAY_STATISTICS({ PacketStatistics(it) }),
        PLAY_SPAWN_ENTITY({ PacketSpawnObject(it) }, isThreadSafe = false),
        PLAY_TITLE({ TitlePacketFactory.createPacket(it) }),
        PLAY_ENTITY_INITIALISATION({ PacketEntityInitialisation(it) }, isThreadSafe = false),
        PLAY_SET_COMPRESSION({ PacketSetCompression(it) }, isThreadSafe = false),
        PLAY_ADVANCEMENT_PROGRESS({ TODO() }),
        PLAY_SCULK_VIBRATION_SIGNAL({ PacketSculkVibrationSignal(it) }),
        ;

        init {
            //  if (playFactory == null && statusFactory == null) {
            //      throw IllegalStateException("Both factories are null!")
            //  } else if (playFactory != null && statusFactory != null) {
            //     throw IllegalStateException("Both factories are not null!")
            //  }
        }


        val state: ConnectionStates = ConnectionStates.valueOf(name.split("_".toRegex()).toTypedArray()[0])
    }
}
