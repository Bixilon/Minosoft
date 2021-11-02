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
import de.bixilon.minosoft.protocol.packets.c2s.handshaking.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.EncryptionResponseC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.LoginPluginResponseC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.LoginStartC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.*
import de.bixilon.minosoft.protocol.packets.c2s.play.advancement.tab.AdvancementTabC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.crafting.CraftingRecipeRequestC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.crafting.DisplayRecipeSetC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.crafting.RecipeBookStateC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.BaseInteractEntityC2SP
import de.bixilon.minosoft.protocol.packets.c2s.status.StatusPingC2SP
import de.bixilon.minosoft.protocol.packets.c2s.status.StatusRequestC2SP
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.StatusS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.login.*
import de.bixilon.minosoft.protocol.packets.s2c.login.CompressionSetS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.*
import de.bixilon.minosoft.protocol.packets.s2c.play.border.*
import de.bixilon.minosoft.protocol.packets.s2c.play.bossbar.BossbarS2CPF
import de.bixilon.minosoft.protocol.packets.s2c.play.combat.CombatEventEndS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.combat.CombatEventEnterS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.combat.CombatEventKillS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.combat.CombatEventS2CF
import de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.ObjectivePositionSetS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.objective.ScoreboardObjectiveS2CF
import de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.score.ScoreboardScoreS2CF
import de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.teams.TeamsS2CF
import de.bixilon.minosoft.protocol.packets.s2c.play.title.*
import de.bixilon.minosoft.protocol.packets.s2c.status.ServerStatusResponseS2CP
import de.bixilon.minosoft.protocol.packets.s2c.status.StatusPongS2CP

class PacketTypes {

    enum class C2S(val clazz: Class<out C2SPacket>? = null) {
        HANDSHAKING_HANDSHAKE(HandshakeC2SP::class.java),
        STATUS_PING(StatusPingC2SP::class.java),
        STATUS_REQUEST(StatusRequestC2SP::class.java),
        LOGIN_LOGIN_START(LoginStartC2SP::class.java),
        LOGIN_ENCRYPTION_RESPONSE(EncryptionResponseC2SP::class.java),
        LOGIN_PLUGIN_RESPONSE(LoginPluginResponseC2SP::class.java),
        PLAY_TELEPORT_CONFIRM(TeleportConfirmC2SP::class.java),
        PLAY_QUERY_BLOCK_NBT,
        PLAY_SET_DIFFICULTY,
        PLAY_CHAT_MESSAGE(ChatMessageC2SP::class.java),
        PLAY_CLIENT_ACTION(ClientActionC2SP::class.java),
        PLAY_CLIENT_SETTINGS(ClientSettingsC2SP::class.java),
        PLAY_AUTOCOMPLETIONS(AutocompletionsC2SP::class.java),
        PLAY_CONTAINER_ACTION_STATUS(ContainerActionStatusC2SP::class.java),
        PLAY_CONTAINER_CLICK_BUTTON(ContainerClickButtonC2SP::class.java),
        PLAY_CONTAINER_SLOT_CLICK(ContainerSlotClickC2SP::class.java),
        PLAY_CONTAINER_CLOSE(ContainerCloseC2SP::class.java),
        PLAY_PLUGIN_MESSAGE(PluginMessageC2SP::class.java),
        PLAY_EDIT_BOOK,
        PLAY_ENTITY_NBT_REQUEST(EntityNBTRequestC2SP::class.java),
        PLAY_INTERACT_ENTITY(BaseInteractEntityC2SP::class.java),
        PLAY_HEARTBEAT(HeartbeatC2SP::class.java),
        PLAY_LOCK_DIFFICULTY,
        PLAY_POSITION(PositionC2SP::class.java),
        PLAY_POSITION_AND_ROTATION(PositionAndRotationC2SP::class.java),
        PLAY_ROTATION(RotationC2SP::class.java),
        PLAY_VEHICLE_MOVE(VehicleMoveC2SP::class.java),
        PLAY_BOAT_STEER(BoatSteerC2SP::class.java),
        PLAY_PICK_ITEM,
        PLAY_CRAFTING_RECIPE_REQUEST(CraftingRecipeRequestC2SP::class.java),
        PLAY_FLY_TOGGLE(FlyToggleC2SP::class.java),
        PLAY_PLAYER_ACTION(PlayerActionC2SP::class.java),
        PLAY_ENTITY_ACTION(EntityActionC2SP::class.java),
        PLAY_VEHICLE_STEER(VehicleSteerC2SP::class.java),
        PLAY_RECIPE_BOOK_STATE(RecipeBookStateC2SP::class.java),
        PLAY_ANVIL_NAME_SET(AnvilNameSetC2SP::class.java),
        PLAY_RESOURCE_PACK_STATUS(ResourcePackStatusC2SP::class.java),
        PLAY_ADVANCEMENT_TAB(AdvancementTabC2SP::class.java),
        PLAY_TRADE_SELECT(TradeSelectC2SP::class.java),
        PLAY_BEACON_EFFECT_SET(BeaconEffectSetC2SP::class.java),
        PLAY_HOTBAR_SLOT_SET(HotbarSlotSetC2SP::class.java),
        PLAY_UPDATE_COMMAND_BLOCK(CommandBlockSetC2SP::class.java),
        PLAY_ITEM_STACK_CREATE(ItemStackCreateC2SP::class.java),
        PLAY_UPDATE_JIGSAW_BLOCK(UpdateJigsawBlockC2SP::class.java),
        PLAY_UPDATE_STRUCTURE_BLOCK(UpdateStructureBlockC2SP::class.java),
        PLAY_SIGN_TEXT_SET(SignTextSetC2SP::class.java),
        PLAY_ARM_SWING(ArmSwingC2SP::class.java),
        PLAY_ENTITY_SPECTATE(EntitySpectateC2SP::class.java),
        PLAY_BLOCK_INTERACT(BlockInteractC2SP::class.java),
        PLAY_ITEM_USE(ItemUseC2SP::class.java),
        PLAY_MINECART_COMMAND_BLOCK_SET(MinecartCommandBlockSetC2SP::class.java),
        PLAY_GENERATE_STRUCTURE(StructureGenerateC2SP::class.java),
        PLAY_DISPLAYED_RECIPE_SET(DisplayRecipeSetC2SP::class.java),
        PLAY_PLAYER_GROUND_CHANGE,
        PLAY_PREPARE_CRAFTING_GRID,
        PLAY_QUERY_ENTITY_NBT,
        PLAY_PONG(PongC2SP::class.java)
        ;

        val state: ProtocolStates = ProtocolStates.valueOf(name.split("_".toRegex()).toTypedArray()[0])

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
        STATUS_RESPONSE(statusFactory = { ServerStatusResponseS2CP(it) }, isThreadSafe = false),
        STATUS_PONG(statusFactory = { StatusPongS2CP(it) }, isThreadSafe = false),
        LOGIN_KICK({ LoginKickS2CP(it) }, isThreadSafe = false),
        LOGIN_ENCRYPTION_REQUEST({ EncryptionRequestS2CP(it) }, isThreadSafe = false, errorHandler = EncryptionRequestS2CP),
        LOGIN_LOGIN_SUCCESS({ LoginSuccessS2CP(it) }, isThreadSafe = false),
        LOGIN_COMPRESSION_SET({ CompressionSetS2CP(it) }, isThreadSafe = false),
        LOGIN_PLUGIN_REQUEST({ PacketLoginPluginRequest(it) }),
        PLAY_MOB_SPAWN({ MobSpawnS2CP(it) }, isThreadSafe = false),
        PLAY_EXPERIENCE_ORB_SPAWN({ ExperienceOrbSpawnS2CP(it) }, isThreadSafe = false),
        PLAY_GLOBAL_ENTITY_SPAWN({ GlobalEntitySpawnS2CP(it) }, isThreadSafe = false),
        PLAY_PAINTING_SPAWN({ PaintingSpawnS2CP(it) }, isThreadSafe = false),
        PLAY_PLAYER_ENTITY_SPAWN({ PlayerEntitySpawnS2CP(it) }, isThreadSafe = false),
        PLAY_ENTITY_ANIMATION({ EntityAnimationS2CP(it) }),
        PLAY_STATS_RESPONSE({ PacketStatistics(it) }),
        PLAY_BLOCK_BREAK_ACK({ BlockBreakAckS2CP(it) }),
        PLAY_BLOCK_BREAK_ANIMATION({ BlockBreakAnimationS2CP(it) }),
        PLAY_BLOCK_ENTITY_META_DATA({ BlockEntityMetaDataS2CP(it) }),
        PLAY_BLOCK_ACTION({ BlockActionS2CP(it) }),
        PLAY_BLOCK_SET({ BlockSetS2CP(it) }),
        PLAY_BOSS_BAR({ BossbarS2CPF.createPacket(it) }, isThreadSafe = false),
        PLAY_SERVER_DIFFICULTY({ ServerDifficultyS2CP(it) }),
        PLAY_CHAT_MESSAGE({ ChatMessageS2CP(it) }, isThreadSafe = false),
        PLAY_MASS_BLOCK_SET({ MassBlockSetS2CP(it) }),
        PLAY_AUTOCOMPLETIONS({ AutocompletionsS2CP(it) }),
        PLAY_DECLARE_COMMANDS({ PacketDeclareCommands(it) }),
        PLAY_CONTAINER_ACTION_STATUS({ ContainerActionStatusS2CP(it) }),
        PLAY_CONTAINER_CLOSE({ ContainerCloseS2CP(it) }),
        PLAY_CONTAINER_ITEMS_SET({ ContainerItemsSetS2CP(it) }),
        PLAY_CONTAINER_PROPERTY_SET({ ContainerPropertySetS2CP(it) }),
        PLAY_CONTAINER_ITEM_SET({ ContainerItemSetS2CP(it) }),
        PLAY_ITEM_COOLDOWN_SET({ ItemCooldownSetS2CP(it) }),
        PLAY_PLUGIN_MESSAGE({ PluginMessageS2CP(it) }),
        PLAY_NAMED_SOUND_EVENT({ NamedSoundEventS2CP(it) }),
        PLAY_KICK({ KickS2CP(it) }, isThreadSafe = false),
        PLAY_ENTITY_STATUS({ EntityStatusS2CP(it) }),
        PLAY_EXPLOSION({ ExplosionS2CP(it) }),
        PLAY_CHUNK_UNLOAD({ ChunkUnloadS2CP(it) }),
        PLAY_GAME_EVENT({ GameEventS2CP(it) }),
        PLAY_HORSE_CONTAINER_OPEN({ HorseContainerOpenS2CP(it) }),
        PLAY_HEARTBEAT({ HeartbeatS2CP(it) }),
        PLAY_CHUNK_DATA({ ChunkDataS2CP(it) }),
        PLAY_WORLD_EVENT({ WorldEventS2CP(it) }),
        PLAY_PARTICLE({ ParticleS2CP(it) }),
        PLAY_CHUNK_LIGHT_DATA({ ChunkLightDataS2CP(it) }),
        PLAY_JOIN_GAME({ JoinGameS2CP(it) }, isThreadSafe = false, errorHandler = JoinGameS2CP),
        PLAY_MAP_DATA({ PacketMapData(it) }),
        PLAY_VILLAGER_TRADES({ VillagerTradesS2CP(it) }),
        PLAY_ENTITY_MOVE_AND_ROTATE({ EntityMoveAndRotateS2CP(it) }),
        PLAY_ENTITY_ROTATION({ EntityRotationS2CP(it) }),
        PLAY_ENTITY_RELATIVE_MOVE({ EntityRelativeMoveS2CP(it) }),
        PLAY_VEHICLE_MOVE({ VehicleMoveS2CP(it) }),
        PLAY_BOOK_OPEN({ BookOpenS2CP(it) }),
        PLAY_CONTAINER_OPEN({ ContainerOpenS2CP(it) }),
        PLAY_SIGN_EDITOR_OPEN({ SignEditorOpenS2CP(it) }),
        PLAY_CRAFTING_RECIPE_RESPONSE({ CraftingRecipeResponseS2CP(it) }),
        PLAY_PLAYER_ABILITIES({ PlayerAbilitiesS2CP(it) }),
        PLAY_COMBAT_EVENT({ CombatEventS2CF.createPacket(it) }),
        PLAY_COMBAT_EVENT_END({ CombatEventEndS2CP(it) }),
        PLAY_COMBAT_EVENT_ENTER({ CombatEventEnterS2CP() }),
        PLAY_COMBAT_EVENT_KILL({ CombatEventKillS2CP(it) }),
        PLAY_TAB_LIST_DATA({ TabListDataS2CP(it) }, isThreadSafe = false),
        PLAY_PLAYER_FACE({ PlayerFaceS2CP(it) }),
        PLAY_POSITION_AND_ROTATION({ PositionAndRotationS2CP(it) }),
        PLAY_UNLOCK_RECIPES({ PacketUnlockRecipes(it) }),
        PLAY_ENTITY_DESTROY({ EntityDestroyS2CP(it) }, isThreadSafe = false),
        PLAY_ENTITY_STATUS_EFFECT_REMOVE({ EntityStatusEffectRemoveS2CP(it) }),
        PLAY_RESOURCEPACK_REQUEST({ ResourcepackRequestS2CP(it) }),
        PLAY_RESPAWN({ RespawnS2CP(it) }),
        PLAY_ENTITY_HEAD_ROTATION({ EntityHeadRotationS2CP(it) }),
        PLAY_SELECT_ADVANCEMENT_TAB({ PacketSelectAdvancementTab(it) }),
        PLAY_WORLD_BORDER({ WorldBorderS2CF.createPacket(it) }),
        PLAY_WORLD_BORDER_INITIALIZE({ InitializeWorldBorderS2CPacket(it) }),
        PLAY_CENTER_SET_WORLD_BORDER_({ CenterSetWorldBorderS2CPacket(it) }),
        PLAY_WORLD_BORDER_LERP_SIZE({ LerpSizeWorldBorderS2CPacket(it) }),
        PLAY_WORLD_BORDER_SIZE({ SizeSetWorldBorderS2CPacket(it) }),
        PLAY_WORLD_BORDER_SET_WARN_TIME({ WarningTimeSetWorldBorderS2CPacket(it) }),
        PLAY_WORLD_BORDER_SET_WARN_BLOCKS({ WarningBlocksSetWorldBorderS2CPacket(it) }),
        PLAY_CAMERA({ CameraS2CP(it) }),
        PLAY_HOTBAR_SLOT_SET({ HotbarSlotSetS2CP(it) }),
        PLAY_CHUNK_CENTER_SET({ ChunkCenterSetS2CP(it) }),
        PLAY_OBJECTIVE_POSITION_SET({ ObjectivePositionSetS2CP(it) }, isThreadSafe = false),
        PLAY_ENTITY_METADATA({ EntityMetadataS2CP(it) }, isThreadSafe = false),
        PLAY_ENTITY_ATTACH({ EntityAttachS2CP(it) }),
        PLAY_ENTITY_VELOCITY({ EntityVelocityS2CP(it) }),
        PLAY_ENTITY_EQUIPMENT({ EntityEquipmentS2CP(it) }),
        PLAY_EXPERIENCE_SET({ ExperienceSetS2CP(it) }),
        PLAY_HEALTH_SET({ HealthSetS2CP(it) }),
        PLAY_SCOREBOARD_OBJECTIVE({ ScoreboardObjectiveS2CF.createPacket(it) }, isThreadSafe = false),
        PLAY_ENTITY_PASSENGER_SET({ EntityPassengerSetS2CP(it) }),
        PLAY_TEAMS({ TeamsS2CF.createPacket(it) }),
        PLAY_UPDATE_SCORE({ ScoreboardScoreS2CF.createPacket(it) }, isThreadSafe = false),
        PLAY_COMPASS_POSITION_SET({ CompassPositionSetS2CP(it) }),
        PLAY_WORLD_TIME_SET({ WorldTimeSetS2CP(it) }),
        PLAY_ENTITY_SOUND_EVENT({ EntitySoundEventS2CP(it) }),
        PLAY_SOUND_EVENT({ SoundEventS2CP(it) }),
        PLAY_STOP_SOUND({ StopSoundS2CP(it) }),
        PLAY_TAB_LIST_TEXT_SET({ TabListTextSetS2CP(it) }),
        PLAY_NBT_QUERY_RESPONSE({ NBTQueryResponseS2CP(it) }),
        PLAY_ENTITY_COLLECT_ANIMATION({ EntityCollectAnimationS2CP(it) }, isThreadSafe = false),
        PLAY_ENTITY_TELEPORT({ EntityTeleportS2CP(it) }, isThreadSafe = false),
        PLAY_ADVANCEMENTS({ PacketAdvancements(it) }),
        PLAY_ENTITY_EFFECT_ATTRIBUTES({ EntityEffectAttributesS2CP(it) }),
        PLAY_ENTITY_STATUS_EFFECT({ EntityStatusEffectS2CP(it) }),
        PLAY_DECLARE_RECIPES({ PacketDeclareRecipes(it) }),
        PLAY_TAGS({ TagsS2CP(it) }),
        PLAY_BED_USE({ BedUseS2CP(it) }),
        PLAY_VIEW_DISTANCE_SET({ ViewDistanceSetS2CP(it) }),
        PLAY_MASS_CHUNK_DATA({ MassChunkDataS2CP(it) }),
        PLAY_SIGN_TEXT_SET({ SignTextSetS2CP(it) }),
        PLAY_STATISTICS({ PacketStatistics(it) }),
        PLAY_ENTITY_OBJECT_SPAWN({ EntityObjectSpawnS2CP(it) }, isThreadSafe = false),
        PLAY_TITLE({ TitleS2CF.createPacket(it) }, isThreadSafe = false),
        PLAY_TITLE_CLEAR({ TitleS2CF.createClearTitlePacket(it) }, isThreadSafe = false),
        PLAY_HOTBAR_TEXT_SET({ HotbarTextSetS2CP(it) }),
        PLAY_TITLE_SUBTITLE_SET({ TitleSubtitleSetS2CP(it) }, isThreadSafe = false),
        PLAY_TITLE_SET({ TitleSetS2CP(it) }, isThreadSafe = false),
        PLAY_TITLE_TIMES_SET({ TitleTimesSetS2CP(it) }, isThreadSafe = false),
        PLAY_EMPTY_ENTITY_MOVE({ EmptyEntityMoveS2CP(it) }, isThreadSafe = false),
        PLAY_COMPRESSION_SET({ CompressionSetS2CP(it) }, isThreadSafe = false),
        PLAY_ADVANCEMENT_PROGRESS({ TODO() }),
        PLAY_VIBRATION_SIGNAL({ VibrationSignalS2CP(it) }),
        PLAY_PING({ PingS2CP(it) }),
        ;

        init {
            //  if (playFactory == null && statusFactory == null) {
            //      throw IllegalStateException("Both factories are null!")
            //  } else if (playFactory != null && statusFactory != null) {
            //     throw IllegalStateException("Both factories are not null!")
            //  }
        }


        val state: ProtocolStates = ProtocolStates.valueOf(name.split("_".toRegex()).toTypedArray()[0])
    }
}
