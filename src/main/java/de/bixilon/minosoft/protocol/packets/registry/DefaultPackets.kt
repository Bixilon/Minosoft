/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.registry

import de.bixilon.minosoft.protocol.packets.c2s.common.HeartbeatC2SP
import de.bixilon.minosoft.protocol.packets.c2s.common.PongC2SP
import de.bixilon.minosoft.protocol.packets.c2s.common.ResourcepackC2SP
import de.bixilon.minosoft.protocol.packets.c2s.common.SettingsC2SP
import de.bixilon.minosoft.protocol.packets.c2s.configuration.ReadyC2SP
import de.bixilon.minosoft.protocol.packets.c2s.handshake.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.ChannelC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.ConfigureC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.EncryptionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.StartC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.NextChunkBatchC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.ReconfigureC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.SessionDataC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.TradeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.advancement.tab.AdvancementCloseTabC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.advancement.tab.AdvancementOpenTabC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.block.*
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.*
import de.bixilon.minosoft.protocol.packets.c2s.play.container.CloseContainerC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerButtonC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.difficulty.DifficultyC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.difficulty.LockDifficultyC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntityActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntityNbtC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntitySpectateC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityAttackC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityEmptyInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityInteractPositionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.*
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.vehicle.MoveVehicleC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.vehicle.SteerBoatC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.vehicle.VehicleInputC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.*
import de.bixilon.minosoft.protocol.packets.c2s.play.item.BookC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.ItemPickC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.ItemStackCreateC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.recipe.CraftingRecipeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.recipe.DisplayedRecipeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.recipe.RecipeBookC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.recipe.book.DisplayRecipeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.recipe.book.RecipeBookStatesC2SP
import de.bixilon.minosoft.protocol.packets.c2s.status.PingC2SP
import de.bixilon.minosoft.protocol.packets.c2s.status.StatusRequestC2SP
import de.bixilon.minosoft.protocol.packets.s2c.common.*
import de.bixilon.minosoft.protocol.packets.s2c.common.resourcepack.RemoveResourcepackS2CP
import de.bixilon.minosoft.protocol.packets.s2c.common.resourcepack.ResourcepackS2CP
import de.bixilon.minosoft.protocol.packets.s2c.configuration.ReadyS2CP
import de.bixilon.minosoft.protocol.packets.s2c.configuration.RegistriesS2CP
import de.bixilon.minosoft.protocol.packets.s2c.login.ChannelS2CP
import de.bixilon.minosoft.protocol.packets.s2c.login.EncryptionS2CP
import de.bixilon.minosoft.protocol.packets.s2c.login.SuccessS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.*
import de.bixilon.minosoft.protocol.packets.s2c.play.advancement.AdvancementTabS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.advancement.AdvancementsS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.block.*
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.*
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.batch.ChunkBatchDoneS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.batch.ChunkBatchStartS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.border.*
import de.bixilon.minosoft.protocol.packets.s2c.play.bossbar.BossbarS2CF
import de.bixilon.minosoft.protocol.packets.s2c.play.chat.*
import de.bixilon.minosoft.protocol.packets.s2c.play.combat.CombatEventS2CF
import de.bixilon.minosoft.protocol.packets.s2c.play.combat.EndCombatEventS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.combat.EnterCombatEventS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.combat.KillCombatEventS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.container.*
import de.bixilon.minosoft.protocol.packets.s2c.play.entity.*
import de.bixilon.minosoft.protocol.packets.s2c.play.entity.effect.EntityEffectS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.entity.effect.EntityRemoveEffectS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.entity.move.*
import de.bixilon.minosoft.protocol.packets.s2c.play.entity.passenger.EntityAttachS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.entity.passenger.EntityPassengerS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.entity.player.*
import de.bixilon.minosoft.protocol.packets.s2c.play.entity.spawn.*
import de.bixilon.minosoft.protocol.packets.s2c.play.item.BookS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.item.CompassPositionS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.item.CraftingRecipeS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.item.ItemCooldownS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.map.MapS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.map.legacy.LegacyMapS2CF
import de.bixilon.minosoft.protocol.packets.s2c.play.recipes.RecipesS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.recipes.UnlockRecipesS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.ObjectivePositionS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.objective.ObjectiveS2CF
import de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.score.PutScoreboardScoreS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.score.RemoveScoreboardScoreS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.score.ScoreboardScoreS2CF
import de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.teams.TeamsS2CF
import de.bixilon.minosoft.protocol.packets.s2c.play.sign.SignEditorS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.sign.SignTextS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.sound.EntitySoundS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.sound.NamedSoundS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.sound.SoundEventS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.sound.StopSoundS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.tab.LegacyTabListS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.tab.TabListRemoveS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.tab.TabListS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.tab.TabListTextS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.tick.TickRateS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.tick.TickStepS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.title.*
import de.bixilon.minosoft.protocol.packets.s2c.play.world.*
import de.bixilon.minosoft.protocol.packets.s2c.status.PongS2CP
import de.bixilon.minosoft.protocol.packets.s2c.status.StatusS2CP
import de.bixilon.minosoft.protocol.protocol.PacketDirections
import de.bixilon.minosoft.protocol.protocol.ProtocolStates

object DefaultPackets {
    val C2S = mapOf(
        ProtocolStates.HANDSHAKE to PacketRegistry(threadSafe = false, extra = PacketExtraHandler.Disconnect).apply {
            register("handshake", HandshakeC2SP::class)
        },
        ProtocolStates.STATUS to PacketRegistry(threadSafe = false, extra = PacketExtraHandler.Disconnect).apply {
            register("ping", PingC2SP::class)
            register("status_request", StatusRequestC2SP::class)
        },
        ProtocolStates.LOGIN to PacketRegistry(threadSafe = false, extra = PacketExtraHandler.Disconnect).apply {
            register("channel", ChannelC2SP::class)
            register("channel", ChannelC2SP::class)
            register("encryption", EncryptionC2SP::class)
            register("enter_configuration", ConfigureC2SP::class)
            register("start", StartC2SP::class)
            register("configure", ConfigureC2SP::class)
        },
        ProtocolStates.CONFIGURATION to PacketRegistry(threadSafe = false, extra = PacketExtraHandler.Disconnect).apply {
            register("channel", de.bixilon.minosoft.protocol.packets.c2s.common.ChannelC2SP::class)
            register("heartbeat", HeartbeatC2SP::class)
            register("pong", PongC2SP::class)
            register("resourcepack", ResourcepackC2SP::class)
            register("settings", SettingsC2SP::class)

            register("ready", ReadyC2SP::class)
        },
        ProtocolStates.PLAY to PacketRegistry(threadSafe = true).apply {
            register("channel", de.bixilon.minosoft.protocol.packets.c2s.common.ChannelC2SP::class, threadSafe = false)
            register("heartbeat", HeartbeatC2SP::class)
            register("pong", PongC2SP::class)
            register("resourcepack", ResourcepackC2SP::class)
            register("settings", SettingsC2SP::class)

            register("advancement_tab", AdvancementCloseTabC2SP::class)
            register("advancement_tab", AdvancementOpenTabC2SP::class)

            register("anvil_item_name", AnvilItemNameC2SP::class)
            register("beacon_effect", BeaconEffectC2SP::class)
            register("block_interact", BlockInteractC2SP::class)
            register("block_nbt", BlockNbtC2SP::class)
            register("command_block", CommandBlockC2SP::class)
            register("generate_structure", GenerateStructureC2SP::class)
            register("jigsaw_block", JigsawBlockC2SP::class)
            register("minecart_command_block", MinecartCommandBlockC2SP::class)
            register("sign_text", SignTextC2SP::class)
            register("structure_block", StructureBlockC2SP::class)

            register("chat_message", ChatMessageC2SP::class, threadSafe = false)
            register("chat_preview", ChatPreviewC2SP::class, threadSafe = false)
            register("command", CommandC2SP::class, threadSafe = false)
            register("command_suggestions", CommandSuggestionsC2SP::class, threadSafe = false)
            register("legacy_message_acknowledgement", LegacyMessageAcknowledgementC2SP::class, threadSafe = false)
            register("message_acknowledgement", MessageAcknowledgementC2SP::class, threadSafe = false)
            register("signed_chat_message", SignedChatMessageC2SP::class, threadSafe = false)

            register("close_container", CloseContainerC2SP::class)
            register("container_action", ContainerActionC2SP::class)
            register("container_button", ContainerButtonC2SP::class)
            register("container_click", ContainerClickC2SP::class)

            register("difficulty", DifficultyC2SP::class)
            register("lock_difficulty", LockDifficultyC2SP::class)

            register("entity_attack", EntityAttackC2SP::class); register("entity_interact", EntityAttackC2SP::class)
            register("entity_empty_interact", EntityEmptyInteractC2SP::class); register("entity_interact", EntityEmptyInteractC2SP::class)
            register("entity_interact_position", EntityInteractPositionC2SP::class); register("entity_interact", EntityInteractPositionC2SP::class)

            register("move_vehicle", MoveVehicleC2SP::class)
            register("steer_boat", SteerBoatC2SP::class)
            register("vehicle_input", VehicleInputC2SP::class)

            register("confirm_teleport", ConfirmTeleportC2SP::class)
            register("ground_change", GroundChangeC2SP::class)
            register("position", PositionC2SP::class)
            register("position_rotation", PositionRotationC2SP::class)
            register("rotation", RotationC2SP::class)

            register("client_action", ClientActionC2SP::class)
            register("hotbar_slot", HotbarSlotC2SP::class)
            register("player_action", PlayerActionC2SP::class)
            register("swing_arm", SwingArmC2SP::class)
            register("toggle_fly", ToggleFlyC2SP::class)

            register("entity_action", EntityActionC2SP::class)
            register("entity_nbt", EntityNbtC2SP::class)
            register("entity_spectate", EntitySpectateC2SP::class)

            register("book", BookC2SP::class)
            register("item_pick", ItemPickC2SP::class)
            register("item_stack_create", ItemStackCreateC2SP::class)
            register("use_item", UseItemC2SP::class)

            register("display_recipe", DisplayRecipeC2SP::class)
            register("recipe_book_states", RecipeBookStatesC2SP::class)

            register("crafting_recipe", CraftingRecipeC2SP::class)
            register("displayed_recipe", DisplayedRecipeC2SP::class)
            register("recipe_book", RecipeBookC2SP::class)

            register("next_chunk_batch", NextChunkBatchC2SP::class)
            register("ping", de.bixilon.minosoft.protocol.packets.c2s.play.PingC2SP::class)
            register("reconfigure", ReconfigureC2SP::class)
            register("session_data", SessionDataC2SP::class)
            register("trade", TradeC2SP::class)
        },
    )

    val S2C = mapOf(
        ProtocolStates.STATUS to PacketRegistry(threadSafe = false, extra = PacketExtraHandler.Disconnect).apply {
            register("pong", PongS2CP::class, ::PongS2CP)
            register("status", StatusS2CP::class, ::StatusS2CP)
        },
        ProtocolStates.LOGIN to PacketRegistry(threadSafe = false, extra = PacketExtraHandler.Disconnect).apply {
            registerPlay("channel", ::ChannelS2CP, ChannelS2CP::class)
            registerPlay("compression", ::CompressionS2CP, CompressionS2CP::class)
            registerPlay("encryption", ::EncryptionS2CP, EncryptionS2CP::class)
            registerPlay("kick", ::KickS2CP, KickS2CP::class)
            registerPlay("success", ::SuccessS2CP, SuccessS2CP::class)
        },
        ProtocolStates.CONFIGURATION to PacketRegistry(threadSafe = false, extra = PacketExtraHandler.Disconnect).apply {
            registerPlay("channel", { de.bixilon.minosoft.protocol.packets.s2c.common.ChannelS2CP(it) })
            registerPlay("compression", ::CompressionS2CP)
            registerPlay("features", ::FeaturesS2CP)
            registerPlay("heartbeat", ::HeartbeatS2CP)
            registerPlay("kick", ::KickS2CP)
            registerPlay("ping", ::PingS2CP)
            registerPlay("remove_resourcepack", ::RemoveResourcepackS2CP)
            registerPlay("resourcepack", ::ResourcepackS2CP)
            registerPlay("tags", ::TagsS2CP)

            registerPlay("ready", ::ReadyS2CP)
            registerPlay("registries", ::RegistriesS2CP)
        },
        ProtocolStates.PLAY to PacketRegistry(threadSafe = true).apply {
            registerPlay("channel", { de.bixilon.minosoft.protocol.packets.s2c.common.ChannelS2CP(it) }, threadSafe = false)
            registerPlay("compression", ::CompressionS2CP, threadSafe = false)
            registerPlay("features", ::FeaturesS2CP, threadSafe = false)
            registerPlay("heartbeat", ::HeartbeatS2CP)
            registerPlay("kick", ::KickS2CP, threadSafe = false)
            registerPlay("ping", ::PingS2CP)
            registerPlay("remove_resourcepack", ::RemoveResourcepackS2CP)
            registerPlay("resourcepack", ::ResourcepackS2CP)
            registerPlay("tags", ::TagsS2CP, threadSafe = false)

            registerPlay("advancements", ::AdvancementsS2CP, threadSafe = false)
            registerPlay("advancement_tab", ::AdvancementTabS2CP, threadSafe = false)

            registerPlay("block_action", ::BlockActionS2CP, threadSafe = false)
            registerPlay("block_break_animation", ::BlockBreakAnimationS2CP)
            registerPlay("block_break", ::BlockBreakS2CP, threadSafe = false)
            registerPlay("block_data", ::BlockDataS2CP, threadSafe = false)
            registerPlay("block", ::BlockS2CP, threadSafe = false)
            registerPlay("blocks", ::BlocksS2CP, threadSafe = false)
            registerPlay("legacy_block_break", ::LegacyBlockBreakS2CP, threadSafe = false)

            registerPlay("chunk_batch_done", ::ChunkBatchDoneS2CP, threadSafe = false)
            registerPlay("chunk_batch_start", ::ChunkBatchStartS2CP, threadSafe = false)
            registerPlay("chunk_biome", ::ChunkBiomeS2CP, lowPriority = true)
            registerPlay("chunk_center", ::ChunkCenterS2CP)
            registerPlay("chunk_light", ::ChunkLightS2CP, lowPriority = true)
            registerPlay("chunk", ::ChunkS2CP, lowPriority = true)
            registerPlay("chunks", ::ChunksS2CP, lowPriority = true)
            registerPlay("simulation_distance", ::SimulationDistanceS2CP)
            registerPlay("unload_chunk", ::UnloadChunkS2CP, threadSafe = false)
            registerPlay("view_distance", ::ViewDistanceS2CP)

            registerPlay("center_world_border", ::CenterWorldBorderS2CP, threadSafe = false)
            registerPlay("initialize_world_border", ::InitializeWorldBorderS2CP, threadSafe = false)
            registerPlay("interpolate_world_border", ::InterpolateWorldBorderS2CP, threadSafe = false)
            registerPlay("size_world_border", ::SizeWorldBorderS2CP, threadSafe = false)
            registerPlay("warn_blocks_world_border", ::WarnBlocksWorldBorderS2CP, threadSafe = false)
            registerPlay("warn_time_world_border", ::WarnTimeWorldBorderS2CP, threadSafe = false)
            registerPlay("world_border", WorldBorderS2CF, threadSafe = false)

            registerPlay("bossbar", BossbarS2CF, threadSafe = false)

            registerPlay("chat_message", ::ChatMessageS2CP, threadSafe = false)
            registerPlay("chat_preview", ::ChatPreviewS2CP, threadSafe = false)
            registerPlay("chat_suggestions", ::ChatSuggestionsS2CP, threadSafe = false)
            registerPlay("commands", ::CommandsS2CP, threadSafe = false)
            registerPlay("command_suggestions", ::CommandSuggestionsS2CP, threadSafe = false)
            registerPlay("hide_message", ::HideMessageS2CP)
            registerPlay("message_header", ::MessageHeaderS2CP, threadSafe = false)
            registerPlay("signed_chat_message", ::SignedChatMessageS2CP, threadSafe = false)
            registerPlay("temporary_chat_preview", ::TemporaryChatPreviewS2CP)
            registerPlay("unsigned_chat_message", ::UnsignedChatMessageS2CP, threadSafe = false)

            registerPlay("combat_event", CombatEventS2CF)
            registerPlay("end_combat_event", ::EndCombatEventS2CP)
            registerPlay("enter_combat_event", ::EnterCombatEventS2CP)
            registerPlay("kill_combat_event", ::KillCombatEventS2CP)

            registerPlay("close_container", ::CloseContainerS2CP, threadSafe = false)
            registerPlay("container_action", ::ContainerActionS2CP, threadSafe = false)
            registerPlay("container_item", ::ContainerItemS2CP, threadSafe = false)
            registerPlay("container_items", ::ContainerItemsS2CP, threadSafe = false)
            registerPlay("container_properties", ::ContainerPropertiesS2CP, threadSafe = false)
            registerPlay("crafter_slot_lock", ::CrafterSlotLockS2CP, threadSafe = false)
            registerPlay("open_container", ::OpenContainerS2CP, threadSafe = false)
            registerPlay("open_entity_container", ::OpenEntityContainerS2CP, threadSafe = false)

            registerPlay("entity_effect", ::EntityEffectS2CP, threadSafe = false)
            registerPlay("entity_remove_effect", ::EntityRemoveEffectS2CP, threadSafe = false)
            registerPlay("empty_move", ::EmptyMoveS2CP, threadSafe = false)
            registerPlay("head_rotation", ::HeadRotationS2CP, threadSafe = false)
            registerPlay("movement_rotation", ::MovementRotationS2CP, threadSafe = false)
            registerPlay("move_vehicle", ::MoveVehicleS2CP, threadSafe = false)
            registerPlay("player_face", ::PlayerFaceS2CP, threadSafe = false)
            registerPlay("position_rotation", ::PositionRotationS2CP, threadSafe = false)
            registerPlay("relative_move", ::RelativeMoveS2CP, threadSafe = false)
            registerPlay("rotation", ::RotationS2CP, threadSafe = false)
            registerPlay("teleport", ::TeleportS2CP, threadSafe = false)
            registerPlay("velocity", ::VelocityS2CP, threadSafe = false)

            registerPlay("entity_attach", ::EntityAttachS2CP, threadSafe = false)
            registerPlay("entity_passenger", ::EntityPassengerS2CP, threadSafe = false)

            registerPlay("camera", ::CameraS2CP, threadSafe = false)
            registerPlay("experience", ::ExperienceS2CP, threadSafe = false)
            registerPlay("health", ::HealthS2CP, threadSafe = false)
            registerPlay("hotbar_slot", ::HotbarSlotS2CP, threadSafe = false)
            registerPlay("player_abilities", ::PlayerAbilitiesS2CP, threadSafe = false)

            registerPlay("entity_destroy", ::EntityDestroyS2CP, threadSafe = false)
            registerPlay("entity_experience_orb", ::EntityExperienceOrbS2CP, threadSafe = false)
            registerPlay("entity_mob_spawn", ::EntityMobSpawnS2CP, threadSafe = false)
            registerPlay("entity_object_spawn", ::EntityObjectSpawnS2CP, threadSafe = false)
            registerPlay("entity_painting", ::EntityPaintingS2CP, threadSafe = false)
            registerPlay("entity_player", ::EntityPlayerS2CP, threadSafe = false)
            registerPlay("global_entity_spawn", ::GlobalEntitySpawnS2CP, threadSafe = false)

            registerPlay("damage_tilt", ::DamageTiltS2CP)
            registerPlay("entity_animation", ::EntityAnimationS2CP)
            registerPlay("entity_attributes", ::EntityAttributesS2CP, threadSafe = false)
            registerPlay("entity_collect", ::EntityCollectS2CP)
            registerPlay("entity_damage", ::EntityDamageS2CP)
            registerPlay("entity_data", ::EntityDataS2CP, threadSafe = false)
            registerPlay("entity_equipment", ::EntityEquipmentS2CP, threadSafe = false)
            registerPlay("entity_event", ::EntityEventS2CP, threadSafe = false)
            registerPlay("entity_sleep", ::EntitySleepS2CP)

            registerPlay("book", ::BookS2CP)
            registerPlay("compass_position", ::CompassPositionS2CP)
            registerPlay("crafting_recipe", ::CraftingRecipeS2CP)
            registerPlay("item_cooldown", ::ItemCooldownS2CP, threadSafe = false)

            registerPlay("legacy_map", LegacyMapS2CF, threadSafe = false)
            registerPlay("map", ::MapS2CP)

            registerPlay("recipes", ::RecipesS2CP)
            registerPlay("unlock_recipes", ::UnlockRecipesS2CP)

            registerPlay("objective", ObjectiveS2CF, threadSafe = false)
            registerPlay("scoreboard_score", ScoreboardScoreS2CF, threadSafe = false)
            registerPlay("put_scoreboard_score", ::PutScoreboardScoreS2CP, threadSafe = false)
            registerPlay("remove_scoreboard_score", ::RemoveScoreboardScoreS2CP, threadSafe = false)
            registerPlay("scoreboard_score", ScoreboardScoreS2CF, threadSafe = false)
            registerPlay("teams", TeamsS2CF, threadSafe = false)
            registerPlay("objective_position", ::ObjectivePositionS2CP, threadSafe = false)

            registerPlay("sign_editor", ::SignEditorS2CP)
            registerPlay("sign_text", ::SignTextS2CP, threadSafe = false)

            registerPlay("entity_sound", ::EntitySoundS2CP)
            registerPlay("named_sound", ::NamedSoundS2CP)
            registerPlay("sound_event", ::SoundEventS2CP)
            registerPlay("stop_sound", ::StopSoundS2CP)

            registerPlay("legacy_tab_list", ::LegacyTabListS2CP, threadSafe = false)
            registerPlay("tab_list_remove", ::TabListRemoveS2CP, threadSafe = false)
            registerPlay("tab_list", ::TabListS2CP, threadSafe = false)
            registerPlay("tab_list_text", ::TabListTextS2CP, threadSafe = false)

            registerPlay("tick_rate", ::TickRateS2CP, threadSafe = false)
            registerPlay("tick_step", ::TickStepS2CP, threadSafe = false)

            registerPlay("clear_title", ClearTitleS2CF, threadSafe = false)
            registerPlay("hotbar_text", ::HotbarTextS2CP, threadSafe = false)
            registerPlay("subtitle", ::SubtitleS2CP, threadSafe = false)
            registerPlay("title", TitleS2CF, threadSafe = false)
            registerPlay("title_text", ::TitleTextS2CP, threadSafe = false)
            registerPlay("title_times", ::TitleTimesS2CP, threadSafe = false)

            registerPlay("difficulty", ::DifficultyS2CP)
            registerPlay("explosion", ::ExplosionS2CP, lowPriority = true)
            registerPlay("particle", ::ParticleS2CP)
            registerPlay("time", ::TimeS2CP)
            registerPlay("vibration", ::VibrationS2CP)
            registerPlay("villager_trades", ::VillagerTradesS2CP)
            registerPlay("world_event", ::WorldEventS2CP)

            registerPlay("bundle", ::BundleS2CP, threadSafe = false)
            registerPlay("game_event", ::GameEventS2CP, threadSafe = false)
            registerPlay("initialize", ::InitializeS2CP, threadSafe = false, extra = PacketExtraHandler.Disconnect)
            registerPlay("nbt_response", ::NbtResponseS2CP)
            registerPlay("play_status", ::PlayStatusS2CP)
            registerPlay("pong", { de.bixilon.minosoft.protocol.packets.s2c.play.PongS2CP(it) })
            registerPlay("reconfigure", ::ReconfigureS2CP, threadSafe = false)
            registerPlay("respawn", ::RespawnS2CP, threadSafe = false, extra = PacketExtraHandler.Disconnect)
            registerPlay("statistics", ::StatisticsS2CP)

        },
    )

    operator fun get(direction: PacketDirections) = when (direction) {
        PacketDirections.CLIENT_TO_SERVER -> C2S
        PacketDirections.SERVER_TO_CLIENT -> S2C
    }
}
