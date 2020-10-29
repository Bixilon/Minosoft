/*
 * Minosoft
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

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.ConfigurationPaths;
import de.bixilon.minosoft.data.GameModes;
import de.bixilon.minosoft.data.entities.Entity;
import de.bixilon.minosoft.data.entities.meta.HumanMetaData;
import de.bixilon.minosoft.data.entities.mob.OtherPlayer;
import de.bixilon.minosoft.data.mappings.blocks.Blocks;
import de.bixilon.minosoft.data.mappings.recipes.Recipes;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.data.player.PingBars;
import de.bixilon.minosoft.data.player.PlayerListItem;
import de.bixilon.minosoft.data.scoreboard.ScoreboardObjective;
import de.bixilon.minosoft.data.scoreboard.ScoreboardScore;
import de.bixilon.minosoft.data.scoreboard.Team;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.data.world.Chunk;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.modding.channels.DefaultPluginChannels;
import de.bixilon.minosoft.modding.event.events.*;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.login.*;
import de.bixilon.minosoft.protocol.packets.clientbound.play.*;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusPong;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusResponse;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketEncryptionResponse;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketKeepAliveResponse;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;
import de.bixilon.minosoft.util.nbt.tag.StringTag;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.UUID;

public class PacketHandler {
    final Connection connection;

    public PacketHandler(Connection connection) {
        this.connection = connection;
    }

    public void handle(PacketStatusResponse pkg) {
        connection.fireEvent(new StatusResponseEvent(connection, pkg));

        // now we know the version, set it, if the config allows it
        Version version;
        int protocolId = connection.getDesiredVersionNumber();
        if (protocolId == -1) {
            protocolId = pkg.getResponse().getProtocolId();
        }
        version = Versions.getVersionByProtocolId(protocolId);
        if (version == null) {
            Log.fatal(String.format("Server is running on unknown version or a invalid version was forced (protocolId=%d, brand=\"%s\")", protocolId, pkg.getResponse().getServerBrand()));
        } else {
            connection.setVersion(version);
        }
        Log.info(String.format("Status response received: %s/%s online. MotD: '%s'", pkg.getResponse().getPlayerOnline(), pkg.getResponse().getMaxPlayers(), pkg.getResponse().getMotd().getANSIColoredMessage()));
        connection.handlePingCallbacks(pkg.getResponse());
    }

    public void handle(PacketStatusPong pkg) {
        connection.fireEvent(new StatusPongEvent(connection, pkg));

        ConnectionPing ping = connection.getConnectionStatusPing();
        if (ping.getPingId() != pkg.getID()) {
            Log.warn(String.format("Server sent unknown ping answer (pingId=%d, expected=%d)", pkg.getID(), ping.getPingId()));
            return;
        }
        long pingDifference = System.currentTimeMillis() - ping.getSendingTime();
        Log.debug(String.format("Pong received (ping=%dms, pingBars=%s)", pingDifference, PingBars.byPing(pingDifference)));
        switch (connection.getReason()) {
            case PING -> connection.disconnect();// pong arrived, closing connection
            case GET_VERSION -> {
                // reconnect...
                connection.disconnect();
                Log.info(String.format("Server is running on version %s (versionId=%d, protocolId=%d), reconnecting...", connection.getVersion().getVersionName(), connection.getVersion().getVersionId(), connection.getVersion().getProtocolId()));
            }
        }
    }

    public void handle(PacketEncryptionRequest pkg) {
        SecretKey secretKey = CryptManager.createNewSharedKey();
        PublicKey publicKey = CryptManager.decodePublicKey(pkg.getPublicKey());
        String serverHash = new BigInteger(CryptManager.getServerHash(pkg.getServerId(), publicKey, secretKey)).toString(16);
        connection.getPlayer().getAccount().join(serverHash);
        connection.sendPacket(new PacketEncryptionResponse(secretKey, pkg.getVerifyToken(), publicKey));
    }

    public void handle(PacketLoginSuccess pkg) {
    }

    public void handle(PacketJoinGame pkg) {
        if (connection.fireEvent(new JoinGameEvent(connection, pkg))) {
            return;
        }

        connection.getPlayer().setGameMode(pkg.getGameMode());
        connection.getPlayer().setPlayer(new OtherPlayer(pkg.getEntityId(), connection.getPlayer().getPlayerName(), connection.getPlayer().getPlayerUUID(), null, null, 0, 0, 0, (short) 0, null));
        connection.getPlayer().getWorld().setHardcore(pkg.isHardcore());
        connection.getMapping().setDimensions(pkg.getDimensions());
        connection.getPlayer().getWorld().setDimension(pkg.getDimension());
        connection.getSender().sendChatMessage("I am alive! ~ Minosoft");
    }

    public void handle(PacketLoginDisconnect pkg) {
        connection.fireEvent(new LoginDisconnectEvent(connection, pkg.getReason()));
        Log.info(String.format("Disconnecting from server (reason=%s)", pkg.getReason().getANSIColoredMessage()));
        connection.disconnect();
    }

    public void handle(PacketPlayerListItem pkg) {
        if (connection.fireEvent(new PlayerListItemChangeEvent(connection, pkg))) {
            return;
        }
        pkg.getPlayerList().forEach((bulk) -> {
            switch (bulk.getAction()) {
                case ADD -> connection.getPlayer().getPlayerList().put(bulk.getUUID(), new PlayerListItem(bulk.getUUID(), bulk.getName(), bulk.getPing(), bulk.getGameMode(), bulk.getDisplayName(), bulk.getProperties()));
                case UPDATE_LATENCY -> {
                    if (bulk.isLegacy()) {
                        //add or update
                        PlayerListItem playerListItem = connection.getPlayer().getPlayerListItem(bulk.getName());
                        if (playerListItem == null) {
                            // create
                            UUID uuid = UUID.randomUUID();
                            connection.getPlayer().getPlayerList().put(uuid, new PlayerListItem(uuid, bulk.getName(), bulk.getPing()));
                        } else {
                            // update ping
                            playerListItem.setPing(bulk.getPing());
                        }
                        return;
                    }
                    connection.getPlayer().getPlayerList().get(bulk.getUUID()).setPing(bulk.getPing());
                }
                case REMOVE_PLAYER -> {
                    if (bulk.isLegacy()) {
                        PlayerListItem playerListItem = connection.getPlayer().getPlayerListItem(bulk.getName());
                        if (playerListItem == null) {
                            // not initialized yet
                            return;
                        }
                        connection.getPlayer().getPlayerList().remove(connection.getPlayer().getPlayerListItem(bulk.getName()).getUUID());
                        return;
                    }
                    connection.getPlayer().getPlayerList().remove(bulk.getUUID());
                }
                case UPDATE_GAMEMODE -> connection.getPlayer().getPlayerList().get(bulk.getUUID()).setGameMode(bulk.getGameMode());
                case UPDATE_DISPLAY_NAME -> connection.getPlayer().getPlayerList().get(bulk.getUUID()).setDisplayName(bulk.getDisplayName());
            }
        });
    }

    public void handle(PacketTimeUpdate pkg) {
        if (connection.fireEvent(new TimeChangeEvent(connection, pkg))) {
            return;
        }

    }

    public void handle(PacketKeepAlive pkg) {
        connection.sendPacket(new PacketKeepAliveResponse(pkg.getId()));
    }

    public void handle(PacketChunkBulk pkg) {
        pkg.getChunks().forEach(((location, chunk) -> connection.fireEvent(new ChunkDataChangeEvent(connection, location, chunk))));

        connection.getPlayer().getWorld().setChunks(pkg.getChunks());
    }

    public void handle(PacketUpdateHealth pkg) {
        connection.fireEvent(new UpdateHealthEvent(connection, pkg));

        connection.getPlayer().setFood(pkg.getFood());
        connection.getPlayer().setHealth(pkg.getHealth());
        connection.getPlayer().setSaturation(pkg.getSaturation());
        if (pkg.getHealth() <= 0.0F) {
            // do respawn
            connection.getSender().respawn();
        }
    }

    public void handle(PacketPluginMessageReceiving pkg) {
        if (pkg.getChannel().equals(DefaultPluginChannels.MC_BRAND.getChangeableIdentifier().get(connection.getVersion().getVersionId()))) {
            InByteBuffer data = pkg.getDataAsBuffer();
            String serverVersion;
            String clientVersion = (Minosoft.getConfig().getBoolean(ConfigurationPaths.NETWORK_FAKE_CLIENT_BRAND) ? "vanilla" : "Minosoft");
            OutByteBuffer toSend = new OutByteBuffer(connection);
            if (connection.getVersion().getVersionId() < 29) {
                // no length prefix
                serverVersion = new String(data.getBytes());
                toSend.writeBytes(clientVersion.getBytes());
            } else {
                // length prefix
                serverVersion = data.readString();
                toSend.writeString(clientVersion);
            }
            Log.info(String.format("Server is running \"%s\", connected with %s", serverVersion, connection.getVersion().getVersionName()));

            connection.getSender().sendPluginMessageData(DefaultPluginChannels.MC_BRAND.getChangeableIdentifier().get(connection.getVersion().getVersionId()), toSend);
            return;
        }

        // MC|StopSound
        if (pkg.getChannel().equals(DefaultPluginChannels.MC_BRAND.getChangeableIdentifier().get(connection.getVersion().getVersionId()))) {
            // it is basically a packet, handle it like a packet:
            PacketStopSound packet = new PacketStopSound();
            packet.read(pkg.getDataAsBuffer());
            handle(packet);
            return;
        }

        connection.fireEvent(new PluginMessageReceiveEvent(connection, pkg));
    }

    public void handle(PacketSpawnLocation pkg) {
        connection.fireEvent(new SpawnLocationChangeEvent(connection, pkg));
        connection.getPlayer().setSpawnLocation(pkg.getSpawnLocation());
    }

    public void handle(PacketChatMessageReceiving pkg) {
        ChatMessageReceivingEvent event = new ChatMessageReceivingEvent(connection, pkg);
        if (connection.fireEvent(event)) {
            return;
        }
        Log.game("[CHAT] " + event.getMessage());
    }

    public void handle(PacketDisconnect pkg) {
        connection.fireEvent(new LoginDisconnectEvent(connection, pkg));
        // got kicked
        connection.disconnect();
    }

    public void handle(PacketHeldItemChangeReceiving pkg) {
        connection.getPlayer().setSelectedSlot(pkg.getSlot());
    }

    public void handle(PacketSetExperience pkg) {
        if (connection.fireEvent(new ExperienceChangeEvent(connection, pkg))) {
            return;
        }

        connection.getPlayer().setLevel(pkg.getLevel());
        connection.getPlayer().setTotalExperience(pkg.getTotal());
    }

    public void handle(PacketChangeGameState pkg) {
        ChangeGameStateEvent event = new ChangeGameStateEvent(connection, pkg);
        if (connection.fireEvent(event)) {
            return;
        }

        switch (pkg.getReason()) {
            case START_RAIN -> connection.getPlayer().getWorld().setRaining(true);
            case END_RAIN -> connection.getPlayer().getWorld().setRaining(false);
            case CHANGE_GAMEMODE -> connection.getPlayer().setGameMode(GameModes.byId(pkg.getValue().intValue()));
        }
    }

    public void handle(PacketSpawnMob pkg) {
        connection.fireEvent(new EntitySpawnEvent(connection, pkg));

        connection.getPlayer().getWorld().addEntity(pkg.getEntity());
        connection.getVelocityHandler().handleVelocity(pkg.getEntity(), pkg.getVelocity());
    }

    public void handle(PacketEntityMovementAndRotation pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setLocation(pkg.getRelativeLocation());
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setYaw(pkg.getYaw());
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setPitch(pkg.getPitch());
    }

    public void handle(PacketEntityMovement pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setLocation(pkg.getRelativeLocation());
    }

    public void handle(PacketEntityRotation pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setYaw(pkg.getYaw());
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setPitch(pkg.getPitch());
    }

    public void handle(PacketDestroyEntity pkg) {
        connection.fireEvent(new EntityDespawnEvent(connection, pkg));

        for (int entityId : pkg.getEntityIds()) {
            connection.getPlayer().getWorld().removeEntity(entityId);
        }
    }

    public void handle(PacketEntityVelocity pkg) {
        Entity entity;
        if (pkg.getEntityId() == connection.getPlayer().getPlayer().getEntityId()) {
            // that's us!
            entity = connection.getPlayer().getPlayer();
        } else {
            entity = connection.getPlayer().getWorld().getEntity(pkg.getEntityId());
        }
        connection.getVelocityHandler().handleVelocity(entity, pkg.getVelocity());
    }

    public void handle(PacketSpawnPlayer pkg) {
        connection.fireEvent(new EntitySpawnEvent(connection, pkg));

        connection.getPlayer().getWorld().addEntity(pkg.getEntity());
        connection.getVelocityHandler().handleVelocity(pkg.getEntity(), pkg.getVelocity());
    }

    public void handle(PacketEntityTeleport pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setLocation(pkg.getRelativeLocation());
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setYaw(pkg.getYaw());
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setPitch(pkg.getPitch());
    }

    public void handle(PacketEntityHeadRotation pkg) {
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setHeadYaw(pkg.getHeadYaw());
    }

    public void handle(PacketWindowItems pkg) {
        connection.fireEvent(new MultiSlotChangeEvent(connection, pkg));

        connection.getPlayer().setInventory(pkg.getWindowId(), pkg.getData());
    }

    public void handle(PacketEntityMetadata pkg) {
        if (pkg.getEntityId() == connection.getPlayer().getPlayer().getEntityId()) {
            // our own meta data...set it
            connection.getPlayer().getPlayer().setMetaData(pkg.getEntityData(HumanMetaData.class));
            return;
        }
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setMetaData(pkg.getEntityData(connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).getMetaDataClass()));
    }

    public void handle(PacketEntityEquipment pkg) {
        connection.fireEvent(new EntityEquipmentChangeEvent(connection, pkg));

        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).setEquipment(pkg.getSlots());
    }

    public void handle(PacketBlockChange pkg) {
        connection.fireEvent(new BlockChangeEvent(connection, pkg));

        connection.getPlayer().getWorld().setBlock(pkg.getPosition(), pkg.getBlock());
    }

    public void handle(PacketMultiBlockChange pkg) {
        Chunk chunk = connection.getPlayer().getWorld().getChunk(pkg.getLocation());
        if (chunk == null) {
            Log.warn(String.format("Server tried to change blocks in unloaded chunks! (location=%s)", pkg.getLocation()));
            return;
        }
        connection.fireEvent(new MultiBlockChangeEvent(connection, pkg));
        chunk.setBlocks(pkg.getBlocks());
    }

    public void handle(PacketRespawn pkg) {
        if (connection.fireEvent(new RespawnEvent(connection, pkg))) {
            return;
        }

        // clear all chunks
        connection.getPlayer().getWorld().getAllChunks().clear();
        connection.getPlayer().getWorld().setDimension(pkg.getDimension());
        connection.getPlayer().setSpawnConfirmed(false);
        connection.getPlayer().setGameMode(pkg.getGameMode());
    }

    public void handle(PacketOpenSignEditor pkg) {
        OpenSignEditorEvent event = new OpenSignEditorEvent(connection, pkg);
        if (connection.fireEvent(event)) {
            return;
        }
    }

    public void handle(PacketSpawnObject pkg) {
        connection.fireEvent(new EntitySpawnEvent(connection, pkg));

        connection.getPlayer().getWorld().addEntity(pkg.getEntity());
        connection.getVelocityHandler().handleVelocity(pkg.getEntity(), pkg.getVelocity());
    }

    public void handle(PacketSpawnExperienceOrb pkg) {
        connection.fireEvent(new EntitySpawnEvent(connection, pkg));

        connection.getPlayer().getWorld().addEntity(pkg.getEntity());
    }

    public void handle(PacketSpawnWeatherEntity pkg) {
        connection.fireEvent(new EntitySpawnEvent(connection, pkg));
        connection.fireEvent(new LightningBoltSpawnEvent(connection, pkg));
    }

    public void handle(PacketChunkData pkg) {
        pkg.getBlockEntities().forEach(((position, compoundTag) -> connection.fireEvent(new BlockEntityMetaDataChangeEvent(connection, position, null, compoundTag))));
        connection.fireEvent(new ChunkDataChangeEvent(connection, pkg));

        connection.getPlayer().getWorld().setChunk(pkg.getLocation(), pkg.getChunk());
        connection.getPlayer().getWorld().setBlockEntityData(pkg.getBlockEntities());
    }

    public void handle(PacketEntityEffect pkg) {
        if (pkg.getEntityId() == connection.getPlayer().getPlayer().getEntityId()) {
            // that's us!
            connection.getPlayer().getPlayer().addEffect(pkg.getEffect());
            return;
        }
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).addEffect(pkg.getEffect());
    }

    public void handle(PacketRemoveEntityEffect pkg) {
        if (pkg.getEntityId() == connection.getPlayer().getPlayer().getEntityId()) {
            // that's us!
            connection.getPlayer().getPlayer().removeEffect(pkg.getEffect());
            return;
        }
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).removeEffect(pkg.getEffect());
    }

    public void handle(PacketUpdateSignReceiving pkg) {
        CompoundTag nbt = new CompoundTag();
        nbt.writeBlockPosition(pkg.getPosition());
        nbt.writeTag("id", new StringTag("minecraft:sign"));
        for (int i = 0; i < 4; i++) {
            nbt.writeTag(String.format("Text%d", (i + 1)), new StringTag(pkg.getLines()[i].getLegacyText()));
        }
    }

    public void handle(PacketEntityAnimation pkg) {
        // ToDo
    }

    public void handle(PacketEntityStatus pkg) {
        // ToDo
    }

    public void handle(PacketNamedSoundEffect pkg) {
        // ToDo
    }

    public void handle(PacketPlayerAbilitiesReceiving pkg) {
        // ToDo: used to set fly abilities
    }

    public void handle(PacketPlayerPositionAndRotation pkg) {
        // ToDo
    }

    public void handle(PacketAttachEntity pkg) {
        // ToDo check if it is us
        connection.getPlayer().getWorld().getEntity(pkg.getEntityId()).attachTo(pkg.getVehicleId());
        // ToDo leash support
    }

    public void handle(PacketUseBed pkg) {
    }

    public void handle(PacketBlockEntityMetadata pkg) {
        connection.fireEvent(new BlockEntityMetaDataChangeEvent(connection, pkg));
        connection.getPlayer().getWorld().setBlockEntityData(pkg.getPosition(), pkg.getData());
    }

    public void handle(PacketBlockBreakAnimation pkg) {
        BlockBreakAnimationEvent event = new BlockBreakAnimationEvent(connection, pkg);
        if (connection.fireEvent(event)) {
            return;
        }
    }

    public void handle(PacketBlockAction pkg) {
        BlockActionEvent event = new BlockActionEvent(connection, pkg);
        if (connection.fireEvent(event)) {
            return;
        }
    }

    public void handle(PacketExplosion pkg) {
        // remove all blocks set by explosion
        for (byte[] record : pkg.getRecords()) {
            int x = ((int) pkg.getLocation().getX()) + record[0];
            int y = ((int) pkg.getLocation().getY()) + record[1];
            int z = ((int) pkg.getLocation().getZ()) + record[2];
            BlockPosition blockPosition = new BlockPosition(x, (short) y, z);
            connection.getPlayer().getWorld().setBlock(blockPosition, Blocks.nullBlock);
        }
        // ToDo: motion support
    }

    public void handle(PacketCollectItem pkg) {
        if (connection.fireEvent(new CollectItemAnimationEvent(connection, pkg))) {
            return;
        }
        // ToDo
    }

    public void handle(PacketOpenWindow pkg) {
        connection.getPlayer().createInventory(pkg.getInventoryProperties());
    }

    public void handle(PacketCloseWindowReceiving pkg) {
        CloseWindowEvent event = new CloseWindowEvent(connection, pkg);
        if (connection.fireEvent(event)) {
            return;
        }

        connection.getPlayer().deleteInventory(pkg.getWindowId());
    }

    public void handle(PacketSetSlot pkg) {
        connection.fireEvent(new SingleSlotChangeEvent(connection, pkg));

        if (pkg.getWindowId() == -1) {
            // invalid window Id
            // ToDo: what is windowId -1
            return;
        }
        connection.getPlayer().setSlot(pkg.getWindowId(), pkg.getSlotId(), pkg.getSlot());
    }

    public void handle(PacketWindowProperty pkg) {
        // ToDo
    }

    public void handle(PacketConfirmTransactionReceiving pkg) {
        // ToDo
    }

    public void handle(PacketStatistics pkg) {
        // ToDo
    }

    public void handle(PacketTabCompleteReceiving pkg) {
        // ToDo
    }

    public void handle(PacketSpawnPainting pkg) {
        connection.fireEvent(new EntitySpawnEvent(connection, pkg));

        connection.getPlayer().getWorld().addEntity(pkg.getEntity());
    }

    public void handle(PacketParticle pkg) {
        if (connection.fireEvent(new ParticleSpawnEvent(connection, pkg))) {
            return;
        }
    }

    public void handle(PacketEffect pkg) {
        if (connection.fireEvent(new EffectEvent(connection, pkg))) {
            return;
        }
    }

    public void handle(PacketScoreboardObjective pkg) {
        switch (pkg.getAction()) {
            case CREATE -> connection.getPlayer().getScoreboardManager().addObjective(new ScoreboardObjective(pkg.getName(), pkg.getValue()));
            case UPDATE -> connection.getPlayer().getScoreboardManager().getObjective(pkg.getName()).setValue(pkg.getValue());
            case REMOVE -> connection.getPlayer().getScoreboardManager().removeObjective(pkg.getName());
        }
    }

    public void handle(PacketScoreboardUpdateScore pkg) {
        switch (pkg.getAction()) {
            case CREATE_UPDATE -> connection.getPlayer().getScoreboardManager().getObjective(pkg.getScoreName()).addScore(new ScoreboardScore(pkg.getItemName(), pkg.getScoreName(), pkg.getScoreValue()));
            case REMOVE -> {
                ScoreboardObjective objective = connection.getPlayer().getScoreboardManager().getObjective(pkg.getScoreName());
                if (objective == null) {
                    Log.warn(String.format("Server tried to remove score witch was not created before (itemName=\"%s\", scoreName=\"%s\")!", pkg.getItemName(), pkg.getScoreName()));
                } else {
                    objective.removeScore(pkg.getItemName());
                }
            }
        }
    }

    public void handle(PacketScoreboardDisplayScoreboard pkg) {
        // ToDo
    }

    public void handle(PacketTeams pkg) {
        switch (pkg.getAction()) {
            case CREATE -> connection.getPlayer().getScoreboardManager().addTeam(new Team(pkg.getName(), pkg.getDisplayName(), pkg.getPrefix(), pkg.getSuffix(), pkg.isFriendlyFireEnabled(), pkg.isSeeingFriendlyInvisibles(), pkg.getPlayerNames()));
            case INFORMATION_UPDATE -> connection.getPlayer().getScoreboardManager().getTeam(pkg.getName()).updateInformation(pkg.getDisplayName(), pkg.getPrefix(), pkg.getSuffix(), pkg.isFriendlyFireEnabled(), pkg.isSeeingFriendlyInvisibles());
            case REMOVE -> connection.getPlayer().getScoreboardManager().removeTeam(pkg.getName());
            case PLAYER_ADD -> connection.getPlayer().getScoreboardManager().getTeam(pkg.getName()).addPlayers(Arrays.asList(pkg.getPlayerNames()));
            case PLAYER_REMOVE -> connection.getPlayer().getScoreboardManager().getTeam(pkg.getName()).removePlayers(Arrays.asList(pkg.getPlayerNames()));
        }
    }

    public void handle(PacketMapData pkg) {
        // ToDo
    }

    public void handle(PacketLoginSetCompression pkg) {
    }

    public void handle(PacketServerDifficulty pkg) {
    }

    public void handle(PacketTabHeaderAndFooter pkg) {
        if (connection.fireEvent(new PlayerListInfoChangeEvent(connection, pkg))) {
            return;
        }

        connection.getPlayer().setTabHeader(pkg.getHeader());
        connection.getPlayer().setTabFooter(pkg.getFooter());
    }

    public void handle(PacketResourcePackSend pkg) {
        ResourcePackChangeEvent event = new ResourcePackChangeEvent(connection, pkg);
        if (connection.fireEvent(event)) {
            return;
        }
    }

    public void handle(PacketEntityProperties pkg) {
        // ToDo
    }

    public void handle(PacketWorldBorder pkg) {
        // ToDo
    }

    public void handle(PacketTitle pkg) {
        if (connection.fireEvent(new TitleChangeEvent(connection, pkg))) {
            return;
        }

    }

    public void handle(PacketCombatEvent pkg) {
        // ToDo
    }

    public void handle(PacketCamera pkg) {
        // ToDo
    }

    public void handle(PacketUnloadChunk pkg) {
        connection.getPlayer().getWorld().unloadChunk(pkg.getLocation());
    }

    public void handle(PacketSoundEffect pkg) {
        // ToDo
    }

    public void handle(PacketBossBar pkg) {
        BossBarChangeEvent event = new BossBarChangeEvent(connection, pkg);
        if (connection.fireEvent(event)) {
            return;
        }
    }

    public void handle(PacketSetPassenger pkg) {
        // ToDo
    }

    public void handle(PacketSetCooldown pkg) {
    }

    public void handle(PacketCraftRecipeResponse pkg) {
    }

    public void handle(PacketUnlockRecipes pkg) {
    }

    public void handle(PacketSelectAdvancementTab pkg) {
    }

    public void handle(PacketAdvancements pkg) {
    }

    public void handle(PacketNBTQueryResponse pkg) {
    }

    public void handle(PacketFacePlayer pkg) {
    }

    public void handle(PacketTags pkg) {
        //ToDo
    }

    public void handle(PacketDeclareRecipes pkg) {
        Recipes.registerCustomRecipes(pkg.getRecipes());
    }

    public void handle(PacketStopSound pkg) {
    }

    public void handle(PacketUpdateLight pkg) {
    }

    public void handle(PacketUpdateViewPosition pkg) {
    }

    public void handle(PacketUpdateViewDistance pkg) {
    }

    public void handle(PacketOpenHorseWindow pkg) {
    }

    public void handle(PacketTradeList pkg) {
    }

    public void handle(PacketOpenBook pkg) {
    }

    public void handle(PacketAcknowledgePlayerDigging pkg) {
    }

    public void handle(PacketLoginPluginRequest pkg) {
        connection.fireEvent(new LoginPluginMessageRequestEvent(connection, pkg));
    }

    public void handle(PacketEntitySoundEffect pkg) {
    }

    public void handle(PacketSetCompression pkg) {
    }

    public void handle(PacketEntityInitialisation pkg) {
    }

    public void handle(PacketVehicleMovement pkg) {
    }
}
