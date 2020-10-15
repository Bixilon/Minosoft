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

import de.bixilon.minosoft.data.Difficulties;
import de.bixilon.minosoft.data.GameModes;
import de.bixilon.minosoft.data.LevelTypes;
import de.bixilon.minosoft.data.mappings.Dimension;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketRespawn;

public class RespawnEvent extends CancelableEvent {
    private final GameModes gameMode;
    private final Dimension dimension;
    private final Difficulties difficulty;
    private final LevelTypes levelType;

    public RespawnEvent(Connection connection, GameModes gameMode, Dimension dimension, Difficulties difficulty, LevelTypes levelType) {
        super(connection);
        this.gameMode = gameMode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.levelType = levelType;
    }

    public RespawnEvent(Connection connection, PacketRespawn pkg) {
        super(connection);
        this.gameMode = pkg.getGameMode();
        this.dimension = pkg.getDimension();
        this.difficulty = pkg.getDifficulty();
        this.levelType = pkg.getLevelType();
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

    public LevelTypes getLevelType() {
        return levelType;
    }
}
