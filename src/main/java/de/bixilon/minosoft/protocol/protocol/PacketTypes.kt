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
 * You should have received a copy of the GNU General Public License along with this program. If not,
 see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB,
 the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.protocol

import de.bixilon.minosoft.protocol.ErrorHandler
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.handshaking.HandshakeC2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.login.EncryptionResponseC2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.login.LoginPluginResponseC2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.login.LoginStartC2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.*
import de.bixilon.minosoft.protocol.packets.c2s.play.interact.BaseInteractEntityC2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.status.StatusPingC2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.status.StatusRequestC2SPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.StatusS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.login.*
import de.bixilon.minosoft.protocol.packets.s2c.play.*
import de.bixilon.minosoft.protocol.packets.s2c.play.border.*
import de.bixilon.minosoft.protocol.packets.s2c.play.combat.CombatEventS2CPFactory
import de.bixilon.minosoft.protocol.packets.s2c.play.combat.EndCombatEventS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.play.combat.EnterCombatEventS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.play.combat.EntityDeathCombatEventS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.play.title.*
import de.bixilon.minosoft.protocol.packets.s2c.status.PacketStatusPong
import de.bixilon.minosoft.protocol.packets.s2c.status.PacketStatusResponse

class PacketTypes {

    enum class C2S(val clazz: Class<out C2SPacket>? = null) {
        HANDSHAKING_HANDSHAKE(HandshakeC2SPacket::class.java),
        STATUS_PING(StatusPingC2SPacket::class.java),
        STATUS_REQUEST(StatusRequestC2SPacket::class.java),
        LOGIN_LOGIN_START(LoginStartC2SPacket::class.java),
        LOGIN_ENCRYPTION_RESPONSE(EncryptionResponseC2SPacket::class.java),
        LOGIN_PLUGIN_RESPONSE(LoginPluginResponseC2SPacket::class.java),
        PLAY_TELEPORT_CONFIRMATION(TeleportConfirmC2SPacket::class.java),
        PLAY_QUERY_BLOCK_NBT,
        PLAY_SET_DIFFICULTY,
        PLAY_CHAT_MESSAGE(ChatMessageC2SPacket::class.java),
        PLAY_CLIENT_ACTION(ClientActionC2SPacket::class.java),
        PLAY_CLIENT_SETTINGS(ClientSettingsC2SPacket::class.java),
        PLAY_TAB_COMPLETE(TabCompleteC2SPacket::class.java),
        PLAY_WINDOW_CONFIRMATION(WindowConfirmationC2SPacket::class.java),
        PLAY_CLICK_WINDOW_ACTION(ClickWindowActionC2SPacket::class.java),
        PLAY_CLICK_WINDOW_SLOT(ClickWindowSlotC2SPacket::class.java),
        PLAY_CLOSE_WINDOW(CloseWindowC2SPacket::class.java),
        PLAY_PLUGIN_MESSAGE(PluginMessageC2SPacket::class.java),
        PLAY_EDIT_BOOK,
        PLAY_ENTITY_NBT_REQUEST(EntityNBTRequestC2SPacket::class.java),
        PLAY_INTERACT_ENTITY(BaseInteractEntityC2SPacket::class.java),
        PLAY_KEEP_ALIVE(KeepAliveC2SPacket::class.java),
        PLAY_LOCK_DIFFICULTY,
        PLAY_PLAYER_POSITION(PlayerPositionC2SPacket::class.java),
        PLAY_PLAYER_POSITION_AND_ROTATION(PlayerPositionAndRotationC2SPacket::class.java),
        PLAY_PLAYER_ROTATION(PlayerRotationC2SPacket::class.java),
        PLAY_VEHICLE_MOVE(PacketVehicleMovementC2SPacket::class.java),
        PLAY_STEER_BOAT(SteerBoatC2SPacket::class.java),
        PLAY_PICK_ITEM,
        PLAY_CRAFTING_RECIPE_REQUEST(CraftingRecipeRequestC2SPacket::class.java),
        PLAY_PLAYER_ABILITIES(PlayerAbilitiesC2SPacket::class.java),
        PLAY_PLAYER_DIGGING(PlayerDiggingC2SPacket::class.java),
        PLAY_ENTITY_ACTION(EntityActionC2SPacket::class.java),
        PLAY_STEER_VEHICLE(SteerVehicleC2SPacket::class.java),
        PLAY_RECIPE_BOOK_STATE(RecipeBookStateC2SPacket::class.java),
        PLAY_NAME_ITEM(NameItemC2SPacket::class.java),
        PLAY_RESOURCE_PACK_STATUS(ResourcePackStatusC2SPacket::class.java),
        PLAY_ADVANCEMENT_TAB(AdvancementTabC2SPacket::class.java),
        PLAY_SELECT_TRADE(SelectTradeC2SPacket::class.java),
        PLAY_SET_BEACON_EFFECT(SetBeaconEffectC2SPacket::class.java),
        PLAY_HELD_ITEM_CHANGE(HeldItemChangeC2SPacket::class.java),
        PLAY_UPDATE_COMMAND_BLOCK(UpdateCommandBlockC2SPacket::class.java),
        PLAY_CREATIVE_INVENTORY_ACTION(CreativeInventoryActionC2SPacket::class.java),
        PLAY_UPDATE_JIGSAW_BLOCK(UpdateJigsawBlockC2SPacket::class.java),
        PLAY_UPDATE_STRUCTURE_BLOCK(UpdateStructureBlockC2SPacket::class.java),
        PLAY_UPDATE_SIGN(UpdateSignC2SPacket::class.java),
        PLAY_HAND_ANIMATION(HandAnimationC2SPacket::class.java),
        PLAY_SPECTATE_ENTITY(SpectateEntityC2SPacket::class.java),
        PLAY_PLACE_BLOCK(PlaceBlockC2SPacket::class.java),
        PLAY_USE_ITEM(UseItemC2SPacket::class.java),
        PLAY_UPDATE_COMMAND_BLOCK_MINECART(UpdateCommandBlockMinecartC2SPacket::class.java),
        PLAY_GENERATE_STRUCTURE(GenerateStructureC2SPacket::class.java),
        PLAY_SET_DISPLAYED_RECIPE(SetDisplayedRecipeC2SPacket::class.java),
        PLAY_PLAYER_GROUND_CHANGE,
        PLAY_PREPARE_CRAFTING_GRID,
        PLAY_VEHICLE_MOVEMENT,
        PLAY_QUERY_ENTITY_NBT,
        ;

        val state: ConnectionStates = ConnectionStates.valueOf(name.split("_".toRegex()).toTypedArray()[0])

        companion object {
            private val MAPPING: Map<Class<out C2SPacket>, C2S>

            init {
                val mapping: MutableMap<Class<out C2SPacket>, C2S> = mutableMapOf()

                for (value in values()) {
                    if (value.clazz == null) {
                        continue
                    }
                    mapping[value.clazz] = value
                }

                MAPPING = mapping.toMap()
            }

            fun getPacketType(`class`: Class<out C2SPacket>): C2S {
                var checkedClass: Class<*> = `class`

                while (checkedClass != C2SPacket::class.java) {
                    MAPPING[checkedClass]?.let {
                        return it
                    }
                    checkedClass = checkedClass.superclass
                }
                error("Can not find packet type for class $`class`")
            }
        }
    }


    enum class S2C(
        val playFactory: ((buffer: PlayInByteBuffer) -> PlayS2CPacket)? = null,
        val statusFactory: ((buffer: InByteBuffer) -> StatusS2CPacket)? = null,
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
        PLAY_BLOCK_ENTITY_DATA({ BlockEntityMetaDataS2CP(it) }),
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
        PLAY_WORLD_EVENT({ WorldEventS2CP(it) }),
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
        PLAY_COMBAT_EVENT({ CombatEventS2CPFactory.createPacket(it) }),
        PLAY_COMBAT_EVENT_END({ EndCombatEventS2CPacket(it) }),
        PLAY_COMBAT_EVENT_ENTER({ EnterCombatEventS2CPacket() }),
        PLAY_COMBAT_EVENT_KILL({ EntityDeathCombatEventS2CPacket(it) }),
        PLAY_TAB_LIST_ITEM({ PacketTabListItem(it) }),
        PLAY_FACE_PLAYER({ PacketFacePlayer(it) }),
        PLAY_PLAYER_POSITION_AND_ROTATION({ PacketPlayerPositionAndRotation(it) }),
        PLAY_UNLOCK_RECIPES({ PacketUnlockRecipes(it) }),
        PLAY_DESTROY_ENTITIES({ PacketDestroyEntity(it) }),
        PLAY_REMOVE_ENTITY_EFFECT({ PacketRemoveEntityStatusEffect(it) }),
        PLAY_RESOURCE_PACK_SEND({ PacketResourcePackSend(it) }),
        PLAY_RESPAWN({ PacketRespawn(it) }, isThreadSafe = false),
        PLAY_ENTITY_HEAD_ROTATION({ PacketEntityHeadRotation(it) }),
        PLAY_SELECT_ADVANCEMENT_TAB({ PacketSelectAdvancementTab(it) }),
        PLAY_WORLD_BORDER({ WorldBorderS2CFactory.createPacket(it) }),
        PLAY_WORLD_BORDER_INITIALIZE({ InitializeWorldBorderS2CPacket(it) }),
        PLAY_WORLD_BORDER_SET_CENTER({ SetCenterWorldBorderS2CPacket(it) }),
        PLAY_WORLD_BORDER_LERP_SIZE({ LerpSizeWorldBorderS2CPacket(it) }),
        PLAY_WORLD_BORDER_SIZE({ SetSizeWorldBorderS2CPacket(it) }),
        PLAY_WORLD_BORDER_SET_WARN_TIME({ SetWarningTimeWorldBorderS2CPacket(it) }),
        PLAY_WORLD_BORDER_SET_WARN_BLOCKS({ SetWarningBlocksWorldBorderS2CPacket(it) }),
        PLAY_CAMERA({ PacketCamera(it) }),
        PLAY_HELD_ITEM_CHANGE({ PacketHeldItemChangeReceiving(it) }),
        PLAY_UPDATE_VIEW_POSITION({ PacketUpdateViewPosition(it) }),
        PLAY_DISPLAY_SCOREBOARD({ PacketScoreboardDisplayScoreboard(it) }),
        PLAY_ENTITY_METADATA({ PacketEntityMetadata(it) }),
        PLAY_ATTACH_ENTITY({ PacketAttachEntity(it) }),
        PLAY_ENTITY_VELOCITY({ EntityVelocityS2CP(it) }),
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
        PLAY_TITLE({ TitleS2CPFactory.createPacket(it) }),
        PLAY_CLEAR_TITLE({ TitleS2CPFactory.createClearTitlePacket(it) }),
        PLAY_SET_ACTION_BAR_TEXT({ SetActionBarTextS2CPacket(it) }),
        PLAY_SET_ACTION_SUBTITLE({ SetSubTitleS2CPacket(it) }),
        PLAY_SET_TITLE({ SetTitleS2CPacket(it) }),
        PLAY_SET_TIMES({ SetTimesAndDisplayS2CPacket(it) }),
        PLAY_EMPTY_ENTITY_MOVEMENT({ EmptyEntityMovementS2CPacket(it) }, isThreadSafe = false),
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
