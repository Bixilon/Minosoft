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

package de.bixilon.minosoft.modding.event.events;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.Difficulties;
import de.bixilon.minosoft.data.GameModes;
import de.bixilon.minosoft.data.LevelTypes;
import de.bixilon.minosoft.data.mappings.Dimension;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketJoinGame;

public class JoinGameEvent extends CancelableEvent {
    private final int entityId;
    private final boolean hardcore;
    private final GameModes gameMode;
    private final Dimension dimension;
    private final Difficulties difficulty;
    private final int viewDistance;
    private final int maxPlayers;
    private final LevelTypes levelType;
    private final boolean reducedDebugScreen;
    private final boolean enableRespawnScreen;
    private final long hashedSeed;
    private final HashBiMap<ResourceLocation, Dimension> dimensions;

    public JoinGameEvent(Connection connection, int entityId, boolean hardcore, GameModes gameMode, Dimension dimension, Difficulties difficulty, int viewDistance, int maxPlayers, LevelTypes levelType, boolean reducedDebugScreen, boolean enableRespawnScreen, long hashedSeed, HashBiMap<ResourceLocation, Dimension> dimensions) {
        super(connection);
        this.entityId = entityId;
        this.hardcore = hardcore;
        this.gameMode = gameMode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.viewDistance = viewDistance;
        this.maxPlayers = maxPlayers;
        this.levelType = levelType;
        this.reducedDebugScreen = reducedDebugScreen;
        this.enableRespawnScreen = enableRespawnScreen;
        this.hashedSeed = hashedSeed;
        this.dimensions = dimensions;
    }

    public JoinGameEvent(Connection connection, PacketJoinGame pkg) {
        super(connection);
        this.entityId = pkg.getEntityId();
        this.hardcore = pkg.isHardcore();
        this.gameMode = pkg.getGameMode();
        this.dimension = pkg.getDimension();
        this.difficulty = pkg.getDifficulty();
        this.viewDistance = pkg.getViewDistance();
        this.maxPlayers = pkg.getMaxPlayers();
        this.levelType = pkg.getLevelType();
        this.reducedDebugScreen = pkg.isReducedDebugScreen();
        this.enableRespawnScreen = pkg.isEnableRespawnScreen();
        this.hashedSeed = pkg.getHashedSeed();
        this.dimensions = pkg.getDimensions();
    }

    public int getEntityId() {
        return this.entityId;
    }

    public boolean isHardcore() {
        return this.hardcore;
    }

    public GameModes getGameMode() {
        return this.gameMode;
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public Difficulties getDifficulty() {
        return this.difficulty;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public LevelTypes getLevelType() {
        return this.levelType;
    }

    public boolean isReducedDebugScreen() {
        return this.reducedDebugScreen;
    }

    public boolean isEnableRespawnScreen() {
        return this.enableRespawnScreen;
    }

    public long getHashedSeed() {
        return this.hashedSeed;
    }

    public HashBiMap<ResourceLocation, Dimension> getDimensions() {
        return this.dimensions;
    }
}
