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

package de.bixilon.minosoft.modding.event.events;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.game.datatypes.Difficulties;
import de.bixilon.minosoft.game.datatypes.GameModes;
import de.bixilon.minosoft.game.datatypes.LevelTypes;
import de.bixilon.minosoft.game.datatypes.objectLoader.dimensions.Dimension;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketJoinGame;

import java.util.HashMap;

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
    private final HashMap<String, HashBiMap<String, Dimension>> dimensions;

    public JoinGameEvent(Connection connection, int entityId, boolean hardcore, GameModes gameMode, Dimension dimension, Difficulties difficulty, int viewDistance, int maxPlayers, LevelTypes levelType, boolean reducedDebugScreen, boolean enableRespawnScreen, long hashedSeed, HashMap<String, HashBiMap<String, Dimension>> dimensions) {
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
        return entityId;
    }

    public boolean isHardcore() {
        return hardcore;
    }

    public GameModes getGameMode() {
        return gameMode;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public Difficulties getDifficulty() {
        return difficulty;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public LevelTypes getLevelType() {
        return levelType;
    }

    public boolean isReducedDebugScreen() {
        return reducedDebugScreen;
    }

    public boolean isEnableRespawnScreen() {
        return enableRespawnScreen;
    }

    public long getHashedSeed() {
        return hashedSeed;
    }

    public HashMap<String, HashBiMap<String, Dimension>> getDimensions() {
        return dimensions;
    }
}
