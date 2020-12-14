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

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.ConfigurationPaths;
import de.bixilon.minosoft.data.GameModes;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.data.player.PingBars;
import de.bixilon.minosoft.data.player.PlayerListItem;
import de.bixilon.minosoft.data.player.PlayerListItemBulk;
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
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketConfirmTeleport;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketKeepAliveResponse;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketPlayerPositionAndRotationSending;
import de.bixilon.minosoft.protocol.packets.serverbound.status.PacketStatusPing;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;
import de.bixilon.minosoft.util.nbt.tag.StringTag;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.UUID;

public class PacketHandler {
    private final Connection connection;

    public PacketHandler(Connection connection) {
        this.connection = connection;
    }

    public void handle(PacketStatusResponse pkg) {
        this.connection.fireEvent(new StatusResponseEvent(this.connection, pkg));

        // now we know the version, set it, if the config allows it
        Version version;
        int protocolId = -1;
        if (this.connection.getDesiredVersionNumber() != -1) {
            protocolId = Versions.getVersionById(this.connection.getDesiredVersionNumber()).getProtocolId();
        }
        if (protocolId == -1) {
            protocolId = pkg.getResponse().getProtocolId();
        }
        version = Versions.getVersionByProtocolId(protocolId);
        if (version == null) {
            Log.fatal(String.format("Server is running on unknown version or a invalid version was forced (protocolId=%d, brand=\"%s\")", protocolId, pkg.getResponse().getServerBrand()));
        } else {
            this.connection.setVersion(version);
        }
        Log.info(String.format("Status response received: %s/%s online. MotD: '%s'", pkg.getResponse().getPlayerOnline(), pkg.getResponse().getMaxPlayers(), pkg.getResponse().getMotd().getANSIColoredMessage()));
        this.connection.handlePingCallbacks(pkg.getResponse());
        this.connection.setConnectionStatusPing(new ConnectionPing());
        this.connection.sendPacket(new PacketStatusPing(this.connection.getConnectionStatusPing()));
    }

    public void handle(PacketStatusPong pkg) {
        this.connection.fireEvent(new StatusPongEvent(this.connection, pkg));

        ConnectionPing ping = this.connection.getConnectionStatusPing();
        if (ping.getPingId() != pkg.getID()) {
            Log.warn(String.format("Server sent unknown ping answer (pingId=%d, expected=%d)", pkg.getID(), ping.getPingId()));
            return;
        }
        long pingDifference = System.currentTimeMillis() - ping.getSendingTime();
        Log.debug(String.format("Pong received (ping=%dms, pingBars=%s)", pingDifference, PingBars.byPing(pingDifference)));
        switch (this.connection.getReason()) {
            case PING -> this.connection.disconnect();// pong arrived, closing connection
            case GET_VERSION -> {
                // reconnect...
                this.connection.disconnect();
                Log.info(String.format("Server is running on version %s (versionId=%d, protocolId=%d), reconnecting...", this.connection.getVersion().getVersionName(), this.connection.getVersion().getVersionId(), this.connection.getVersion().getProtocolId()));
            }
        }
    }

    public void handle(PacketEncryptionRequest pkg) {
        SecretKey secretKey = CryptManager.createNewSharedKey();
        PublicKey publicKey = CryptManager.decodePublicKey(pkg.getPublicKey());
        String serverHash = new BigInteger(CryptManager.getServerHash(pkg.getServerId(), publicKey, secretKey)).toString(16);
        this.connection.getPlayer().getAccount().join(serverHash);
        this.connection.sendPacket(new PacketEncryptionResponse(secretKey, pkg.getVerifyToken(), publicKey));
    }

    public void handle(PacketLoginSuccess pkg) {
    }

    public void handle(PacketJoinGame pkg) {
        if (this.connection.fireEvent(new JoinGameEvent(this.connection, pkg))) {
            return;
        }

        this.connection.getPlayer().setGameMode(pkg.getGameMode());
        this.connection.getPlayer().getWorld().setHardcore(pkg.isHardcore());
        this.connection.getMapping().setDimensions(pkg.getDimensions());
        this.connection.getPlayer().getWorld().setDimension(pkg.getDimension());
        PlayerEntity entity = new PlayerEntity(this.connection, pkg.getEntityId(), this.connection.getPlayer().getPlayerUUID(), null, null, this.connection.getPlayer().getPlayerName(), null, null);
        this.connection.getPlayer().setEntity(entity);
        this.connection.getPlayer().getWorld().addEntity(entity);
        this.connection.getSender().sendChatMessage("I am alive! ~ Minosoft");
    }

    public void handle(PacketLoginDisconnect pkg) {
        this.connection.fireEvent(new LoginDisconnectEvent(this.connection, pkg.getReason()));
        Log.info(String.format("Disconnecting from server (reason=%s)", pkg.getReason().getANSIColoredMessage()));
        this.connection.disconnect();
    }

    public void handle(PacketPlayerListItem pkg) {
        if (this.connection.fireEvent(new PlayerListItemChangeEvent(this.connection, pkg))) {
            return;
        }
        for (PlayerListItemBulk bulk : pkg.getPlayerList()) {
            PlayerListItem item = this.connection.getPlayer().getPlayerList().get(bulk.getUUID());
            if (item == null && !bulk.isLegacy()) {
                // Aaaaah. Fuck this shit. The server sends us bullshit!
                continue;
            }
            switch (bulk.getAction()) {
                case ADD -> this.connection.getPlayer().getPlayerList().put(bulk.getUUID(), new PlayerListItem(bulk.getUUID(), bulk.getName(), bulk.getPing(), bulk.getGameMode(), bulk.getDisplayName(), bulk.getProperties()));
                case UPDATE_LATENCY -> {
                    if (bulk.isLegacy()) {
                        // add or update
                        if (item == null) {
                            // create
                            UUID uuid = UUID.randomUUID();
                            this.connection.getPlayer().getPlayerList().put(uuid, new PlayerListItem(uuid, bulk.getName(), bulk.getPing()));
                        } else {
                            // update ping
                            item.setPing(bulk.getPing());
                        }
                        continue;
                    }
                    this.connection.getPlayer().getPlayerList().get(bulk.getUUID()).setPing(bulk.getPing());
                }
                case REMOVE_PLAYER -> {
                    if (bulk.isLegacy()) {
                        if (item == null) {
                            // not initialized yet
                            continue;
                        }
                        this.connection.getPlayer().getPlayerList().remove(this.connection.getPlayer().getPlayerListItem(bulk.getName()).getUUID());
                        continue;
                    }
                    this.connection.getPlayer().getPlayerList().remove(bulk.getUUID());
                }
                case UPDATE_GAMEMODE -> item.setGameMode(bulk.getGameMode());
                case UPDATE_DISPLAY_NAME -> item.setDisplayName(bulk.getDisplayName());
            }
        }
    }

    public void handle(PacketTimeUpdate pkg) {
        if (this.connection.fireEvent(new TimeChangeEvent(this.connection, pkg))) {
            return;
        }
    }

    public void handle(PacketKeepAlive pkg) {
        this.connection.sendPacket(new PacketKeepAliveResponse(pkg.getId()));
    }

    public void handle(PacketChunkBulk pkg) {
        pkg.getChunks().forEach(((location, chunk) -> this.connection.fireEvent(new ChunkDataChangeEvent(this.connection, location, chunk))));

        this.connection.getPlayer().getWorld().setChunks(pkg.getChunks());
    }

    public void handle(PacketUpdateHealth pkg) {
        this.connection.fireEvent(new UpdateHealthEvent(this.connection, pkg));

        this.connection.getPlayer().setFood(pkg.getFood());
        this.connection.getPlayer().setHealth(pkg.getHealth());
        this.connection.getPlayer().setSaturation(pkg.getSaturation());
        if (pkg.getHealth() <= 0.0F) {
            // do respawn
            this.connection.getSender().respawn();
        }
    }

    public void handle(PacketPluginMessageReceiving pkg) {
        if (pkg.getChannel().equals(DefaultPluginChannels.MC_BRAND.getChangeableIdentifier().get(this.connection.getVersion().getVersionId()))) {
            InByteBuffer data = pkg.getDataAsBuffer();
            String serverVersion;
            String clientVersion = (Minosoft.getConfig().getBoolean(ConfigurationPaths.BooleanPaths.NETWORK_FAKE_CLIENT_BRAND) ? "vanilla" : "Minosoft");
            OutByteBuffer toSend = new OutByteBuffer(this.connection);
            if (this.connection.getVersion().getVersionId() < 29) {
                // no length prefix
                serverVersion = new String(data.getBytes());
                toSend.writeBytes(clientVersion.getBytes());
            } else {
                // length prefix
                serverVersion = data.readString();
                toSend.writeString(clientVersion);
            }
            Log.info(String.format("Server is running \"%s\", connected with %s", serverVersion, this.connection.getVersion().getVersionName()));

            this.connection.getSender().sendPluginMessageData(DefaultPluginChannels.MC_BRAND.getChangeableIdentifier().get(this.connection.getVersion().getVersionId()), toSend);
            return;
        }

        // MC|StopSound
        if (pkg.getChannel().equals(DefaultPluginChannels.MC_BRAND.getChangeableIdentifier().get(this.connection.getVersion().getVersionId()))) {
            // it is basically a packet, handle it like a packet:
            PacketStopSound packet = new PacketStopSound();
            packet.read(pkg.getDataAsBuffer());
            handle(packet);
            return;
        }

        this.connection.fireEvent(new PluginMessageReceiveEvent(this.connection, pkg));
    }

    public void handle(PacketSpawnLocation pkg) {
        this.connection.fireEvent(new SpawnLocationChangeEvent(this.connection, pkg));
        this.connection.getPlayer().setSpawnLocation(pkg.getSpawnLocation());
    }

    public void handle(PacketChatMessageReceiving pkg) {
        ChatMessageReceivingEvent event = new ChatMessageReceivingEvent(this.connection, pkg);
        if (this.connection.fireEvent(event)) {
            return;
        }
        Log.game(switch (pkg.getPosition()) {
            case SYSTEM_MESSAGE -> "[SYSTEM] ";
            case ABOVE_HOTBAR -> "[HOTBAR] ";
            default -> "[CHAT] ";
        } + event.getMessage());
    }

    public void handle(PacketDisconnect pkg) {
        this.connection.fireEvent(new LoginDisconnectEvent(this.connection, pkg));
        // got kicked
        this.connection.disconnect();
    }

    public void handle(PacketHeldItemChangeReceiving pkg) {
        this.connection.getPlayer().setSelectedSlot(pkg.getSlot());
    }

    public void handle(PacketSetExperience pkg) {
        if (this.connection.fireEvent(new ExperienceChangeEvent(this.connection, pkg))) {
            return;
        }

        this.connection.getPlayer().setLevel(pkg.getLevel());
        this.connection.getPlayer().setTotalExperience(pkg.getTotal());
    }

    public void handle(PacketChangeGameState pkg) {
        ChangeGameStateEvent event = new ChangeGameStateEvent(this.connection, pkg);
        if (this.connection.fireEvent(event)) {
            return;
        }

        Log.game(switch (pkg.getReason()) {
            case START_RAINING -> "Received weather packet: Starting rain...";
            case STOP_RAINING -> "Received weather packet: Stopping rain...";
            case CHANGE_GAMEMODE -> String.format("Received game mode change: Now in %s", GameModes.byId(pkg.getIntValue()));
            default -> "";
        });

        switch (pkg.getReason()) {
            case STOP_RAINING -> this.connection.getPlayer().getWorld().setRaining(false);
            case START_RAINING -> this.connection.getPlayer().getWorld().setRaining(true);
            case CHANGE_GAMEMODE -> this.connection.getPlayer().setGameMode(GameModes.byId(pkg.getIntValue()));
        }
    }

    public void handle(PacketSpawnMob pkg) {
        this.connection.fireEvent(new EntitySpawnEvent(this.connection, pkg));

        this.connection.getPlayer().getWorld().addEntity(pkg.getEntity());
        this.connection.getVelocityHandler().handleVelocity(pkg.getEntity(), pkg.getVelocity());
    }

    public void handle(PacketEntityMovementAndRotation pkg) {
        Entity entity = this.connection.getPlayer().getWorld().getEntity(pkg.getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.setLocation(pkg.getRelativeLocation());
        entity.setRotation(pkg.getYaw(), pkg.getPitch());
    }

    public void handle(PacketEntityMovement pkg) {
        Entity entity = this.connection.getPlayer().getWorld().getEntity(pkg.getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.setLocation(pkg.getRelativeLocation());
    }

    public void handle(PacketEntityRotation pkg) {
        Entity entity = this.connection.getPlayer().getWorld().getEntity(pkg.getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.setRotation(pkg.getYaw(), pkg.getPitch());
    }

    public void handle(PacketDestroyEntity pkg) {
        this.connection.fireEvent(new EntityDespawnEvent(this.connection, pkg));

        for (int entityId : pkg.getEntityIds()) {
            this.connection.getPlayer().getWorld().removeEntity(entityId);
        }
    }

    public void handle(PacketEntityVelocity pkg) {
        Entity entity = this.connection.getPlayer().getWorld().getEntity(pkg.getEntityId());

        if (entity == null) {
            // thanks mojang
            return;
        }
        this.connection.getVelocityHandler().handleVelocity(entity, pkg.getVelocity());
    }

    public void handle(PacketSpawnPlayer pkg) {
        this.connection.fireEvent(new EntitySpawnEvent(this.connection, pkg));

        this.connection.getPlayer().getWorld().addEntity(pkg.getEntity());
        this.connection.getVelocityHandler().handleVelocity(pkg.getEntity(), pkg.getVelocity());
    }

    public void handle(PacketEntityTeleport pkg) {
        Entity entity = this.connection.getPlayer().getWorld().getEntity(pkg.getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.setLocation(pkg.getRelativeLocation());
        entity.setRotation(pkg.getYaw(), pkg.getPitch());
    }

    public void handle(PacketEntityHeadRotation pkg) {
        Entity entity = this.connection.getPlayer().getWorld().getEntity(pkg.getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.setHeadRotation(pkg.getHeadYaw());
    }

    public void handle(PacketWindowItems pkg) {
        this.connection.fireEvent(new MultiSlotChangeEvent(this.connection, pkg));

        this.connection.getPlayer().setInventory(pkg.getWindowId(), pkg.getData());
    }

    public void handle(PacketEntityMetadata pkg) {
        Entity entity = this.connection.getPlayer().getWorld().getEntity(pkg.getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.setMetaData(pkg.getEntityData());
    }

    public void handle(PacketEntityEquipment pkg) {
        this.connection.fireEvent(new EntityEquipmentChangeEvent(this.connection, pkg));

        Entity entity = this.connection.getPlayer().getWorld().getEntity(pkg.getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.setEquipment(pkg.getSlots());
    }

    public void handle(PacketBlockChange pkg) {
        Chunk chunk = this.connection.getPlayer().getWorld().getChunk(pkg.getPosition().getChunkLocation());
        if (chunk == null) {
            // thanks mojang
            return;
        }
        this.connection.fireEvent(new BlockChangeEvent(this.connection, pkg));

        chunk.setBlock(pkg.getPosition().getInChunkLocation(), pkg.getBlock());
    }

    public void handle(PacketMultiBlockChange pkg) {
        Chunk chunk = this.connection.getPlayer().getWorld().getChunk(pkg.getLocation());
        if (chunk == null) {
            // thanks mojang
            return;
        }
        this.connection.fireEvent(new MultiBlockChangeEvent(this.connection, pkg));
        chunk.setBlocks(pkg.getBlocks());
    }

    public void handle(PacketRespawn pkg) {
        if (this.connection.fireEvent(new RespawnEvent(this.connection, pkg))) {
            return;
        }

        // clear all chunks
        this.connection.getPlayer().getWorld().getAllChunks().clear();
        this.connection.getPlayer().getWorld().setDimension(pkg.getDimension());
        this.connection.getPlayer().setSpawnConfirmed(false);
        this.connection.getPlayer().setGameMode(pkg.getGameMode());
    }

    public void handle(PacketOpenSignEditor pkg) {
        OpenSignEditorEvent event = new OpenSignEditorEvent(this.connection, pkg);
        if (this.connection.fireEvent(event)) {
            return;
        }
    }

    public void handle(PacketSpawnObject pkg) {
        this.connection.fireEvent(new EntitySpawnEvent(this.connection, pkg));

        this.connection.getPlayer().getWorld().addEntity(pkg.getEntity());
        this.connection.getVelocityHandler().handleVelocity(pkg.getEntity(), pkg.getVelocity());
    }

    public void handle(PacketSpawnExperienceOrb pkg) {
        this.connection.fireEvent(new EntitySpawnEvent(this.connection, pkg));

        this.connection.getPlayer().getWorld().addEntity(pkg.getEntity());
    }

    public void handle(PacketSpawnWeatherEntity pkg) {
        this.connection.fireEvent(new EntitySpawnEvent(this.connection, pkg));
        this.connection.fireEvent(new LightningBoltSpawnEvent(this.connection, pkg));
    }

    public void handle(PacketChunkData pkg) {
        pkg.getBlockEntities().forEach(((position, compoundTag) -> this.connection.fireEvent(new BlockEntityMetaDataChangeEvent(this.connection, position, null, compoundTag))));
        this.connection.fireEvent(new ChunkDataChangeEvent(this.connection, pkg));

        this.connection.getPlayer().getWorld().setChunk(pkg.getLocation(), pkg.getChunk());
        this.connection.getPlayer().getWorld().setBlockEntityData(pkg.getBlockEntities());
    }

    public void handle(PacketEntityEffect pkg) {
        Entity entity = this.connection.getPlayer().getWorld().getEntity(pkg.getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.addEffect(pkg.getEffect());
    }

    public void handle(PacketRemoveEntityEffect pkg) {
        Entity entity = this.connection.getPlayer().getWorld().getEntity(pkg.getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.removeEffect(pkg.getEffect());
    }

    public void handle(PacketUpdateSignReceiving pkg) {
        CompoundTag nbt = new CompoundTag();
        nbt.writeBlockPosition(pkg.getPosition());
        nbt.writeTag("id", new StringTag("minecraft:sign"));
        for (int i = 0; i < 4; i++) {
            nbt.writeTag(String.format("Text%d", (i + 1)), new StringTag(pkg.getLines()[i].getLegacyText()));
        }
        // ToDo: handle sign updates
    }

    public void handle(PacketEntityAnimation pkg) {
        // ToDo
    }

    public void handle(PacketEntityEvent pkg) {
        // ToDo
    }

    public void handle(PacketNamedSoundEffect pkg) {
        // ToDo
    }

    public void handle(PacketPlayerAbilitiesReceiving pkg) {
        // ToDo: used to set fly abilities
    }

    public void handle(PacketPlayerPositionAndRotation pkg) {
        // ToDo: GUI should do this
        this.connection.getPlayer().getEntity().setLocation(pkg.getLocation());
        if (this.connection.getVersion().getVersionId() >= 79) {
            this.connection.sendPacket(new PacketConfirmTeleport(pkg.getTeleportId()));
        } else {
            this.connection.sendPacket(new PacketPlayerPositionAndRotationSending(pkg.getLocation(), pkg.getRotation(), pkg.isOnGround()));
        }
    }

    public void handle(PacketAttachEntity pkg) {
        Entity entity = this.connection.getPlayer().getWorld().getEntity(pkg.getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.attachTo(pkg.getVehicleId());
        // ToDo leash support
    }

    public void handle(PacketUseBed pkg) {
    }

    public void handle(PacketBlockEntityMetadata pkg) {
        this.connection.fireEvent(new BlockEntityMetaDataChangeEvent(this.connection, pkg));
        this.connection.getPlayer().getWorld().setBlockEntityData(pkg.getPosition(), pkg.getData());
    }

    public void handle(PacketBlockBreakAnimation pkg) {
        BlockBreakAnimationEvent event = new BlockBreakAnimationEvent(this.connection, pkg);
        if (this.connection.fireEvent(event)) {
            return;
        }
    }

    public void handle(PacketBlockAction pkg) {
        BlockActionEvent event = new BlockActionEvent(this.connection, pkg);
        if (this.connection.fireEvent(event)) {
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
            this.connection.getPlayer().getWorld().setBlock(blockPosition, null);
        }
        // ToDo: motion support
    }

    public void handle(PacketCollectItem pkg) {
        if (this.connection.fireEvent(new CollectItemAnimationEvent(this.connection, pkg))) {
            return;
        }
        // ToDo
    }

    public void handle(PacketOpenWindow pkg) {
        this.connection.getPlayer().createInventory(pkg.getInventoryProperties());
    }

    public void handle(PacketCloseWindowReceiving pkg) {
        CloseWindowEvent event = new CloseWindowEvent(this.connection, pkg);
        if (this.connection.fireEvent(event)) {
            return;
        }

        this.connection.getPlayer().deleteInventory(pkg.getWindowId());
    }

    public void handle(PacketSetSlot pkg) {
        this.connection.fireEvent(new SingleSlotChangeEvent(this.connection, pkg));

        if (pkg.getWindowId() == -1) {
            // thanks mojang
            // ToDo: what is windowId -1
            return;
        }
        this.connection.getPlayer().setSlot(pkg.getWindowId(), pkg.getSlotId(), pkg.getSlot());
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
        this.connection.fireEvent(new EntitySpawnEvent(this.connection, pkg));

        this.connection.getPlayer().getWorld().addEntity(pkg.getEntity());
    }

    public void handle(PacketParticle pkg) {
        if (this.connection.fireEvent(new ParticleSpawnEvent(this.connection, pkg))) {
            return;
        }
    }

    public void handle(PacketEffect pkg) {
        if (this.connection.fireEvent(new EffectEvent(this.connection, pkg))) {
            return;
        }
    }

    public void handle(PacketScoreboardObjective pkg) {
        switch (pkg.getAction()) {
            case CREATE -> this.connection.getPlayer().getScoreboardManager().addObjective(new ScoreboardObjective(pkg.getName(), pkg.getValue()));
            case UPDATE -> this.connection.getPlayer().getScoreboardManager().getObjective(pkg.getName()).setValue(pkg.getValue());
            case REMOVE -> this.connection.getPlayer().getScoreboardManager().removeObjective(pkg.getName());
        }
    }

    public void handle(PacketScoreboardUpdateScore pkg) {
        switch (pkg.getAction()) {
            case CREATE_UPDATE -> this.connection.getPlayer().getScoreboardManager().getObjective(pkg.getScoreName()).addScore(new ScoreboardScore(pkg.getItemName(), pkg.getScoreName(), pkg.getScoreValue()));
            case REMOVE -> {
                ScoreboardObjective objective = this.connection.getPlayer().getScoreboardManager().getObjective(pkg.getScoreName());
                if (objective != null) {
                    // thanks mojang
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
            case CREATE -> this.connection.getPlayer().getScoreboardManager().addTeam(new Team(pkg.getName(), pkg.getDisplayName(), pkg.getPrefix(), pkg.getSuffix(), pkg.isFriendlyFireEnabled(), pkg.isSeeingFriendlyInvisibles(), pkg.getPlayerNames()));
            case INFORMATION_UPDATE -> this.connection.getPlayer().getScoreboardManager().getTeam(pkg.getName()).updateInformation(pkg.getDisplayName(), pkg.getPrefix(), pkg.getSuffix(), pkg.isFriendlyFireEnabled(), pkg.isSeeingFriendlyInvisibles());
            case REMOVE -> this.connection.getPlayer().getScoreboardManager().removeTeam(pkg.getName());
            case PLAYER_ADD -> this.connection.getPlayer().getScoreboardManager().getTeam(pkg.getName()).addPlayers(Arrays.asList(pkg.getPlayerNames()));
            case PLAYER_REMOVE -> this.connection.getPlayer().getScoreboardManager().getTeam(pkg.getName()).removePlayers(Arrays.asList(pkg.getPlayerNames()));
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
        if (this.connection.fireEvent(new PlayerListInfoChangeEvent(this.connection, pkg))) {
            return;
        }

        this.connection.getPlayer().setTabHeader(pkg.getHeader());
        this.connection.getPlayer().setTabFooter(pkg.getFooter());
    }

    public void handle(PacketResourcePackSend pkg) {
        ResourcePackChangeEvent event = new ResourcePackChangeEvent(this.connection, pkg);
        if (this.connection.fireEvent(event)) {
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
        if (this.connection.fireEvent(new TitleChangeEvent(this.connection, pkg))) {
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
        this.connection.getPlayer().getWorld().unloadChunk(pkg.getLocation());
    }

    public void handle(PacketSoundEffect pkg) {
        // ToDo
    }

    public void handle(PacketBossBar pkg) {
        BossBarChangeEvent event = new BossBarChangeEvent(this.connection, pkg);
        if (this.connection.fireEvent(event)) {
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
        // ToDo
    }

    public void handle(PacketDeclareRecipes pkg) {
        this.connection.getRecipes().registerCustomRecipes(pkg.getRecipes());
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
        this.connection.fireEvent(new LoginPluginMessageRequestEvent(this.connection, pkg));
    }

    public void handle(PacketEntitySoundEffect pkg) {
    }

    public void handle(PacketSetCompression pkg) {
    }

    public void handle(PacketEntityInitialisation pkg) {
    }

    public void handle(PacketVehicleMovement pkg) {
    }

    public void handle(PacketDeclareCommands pkg) {
        this.connection.setCommandRootNode(pkg.getRootNode());
        // ToDo: Remove these dummy commands
        String[] commands = {
                "msg Bixilon TestReason 2Paramter 3 4 asd  asd",
                "msg @a[name=Bixilon, level=23, gamemode=!survival] trest asd 12312 sad123123213",
                "help",
                "team list",
                "tasdasda",
                "msg @a[ name = \"Bixilon\" ] asd",
                "msg    @a[ name =     Bixilon            ] asd asdsadasd",
                "msg     @a[ name =     Bixilon    ,team=        ] asd asdsadasd",
                "msg    @a[ name                = Bixilon    ,                      team   =!] asd asdsadasd",
                "give Bixilon minecraft:acacia_boat",
                "give Bixilon minecraft:acacia_boat{asd:12}",
        };
        for (String command : commands) {
            try {
                pkg.getRootNode().isSyntaxCorrect(this.connection, command);
                Log.game("Command \"%s\" is valid", command);
            } catch (CommandParseException e) {
                Log.game("Command \"%s\" is invalid, %s: %s", command, e.getClass().getSimpleName(), e.getErrorMessage());
                e.printStackTrace();
            }
        }
    }
}
