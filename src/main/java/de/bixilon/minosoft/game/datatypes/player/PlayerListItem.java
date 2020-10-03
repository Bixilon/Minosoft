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

package de.bixilon.minosoft.game.datatypes.player;

import de.bixilon.minosoft.game.datatypes.GameModes;
import de.bixilon.minosoft.game.datatypes.TextComponent;

import java.util.HashMap;
import java.util.UUID;

// The holder for the data on <tab>
public class PlayerListItem {
    // required fields
    final UUID uuid;
    final String name;
    final boolean legacy;
    int ping;
    //optional fields
    GameModes gameMode;
    TextComponent displayName;
    HashMap<PlayerProperties, PlayerProperty> properties;

    /**
     * Legacy (1.7.10)
     *
     * @param name Player name
     * @param ping Ping in milliseconds
     */
    public PlayerListItem(UUID uuid, String name, int ping) {
        this.uuid = uuid;
        this.name = name;
        this.ping = ping;
        this.legacy = true;
    }

    public PlayerListItem(UUID uuid, String name, int ping, GameModes gameMode, TextComponent displayName, HashMap<PlayerProperties, PlayerProperty> properties) {
        this.uuid = uuid;
        this.name = name;
        this.ping = ping;
        this.gameMode = gameMode;
        this.displayName = displayName;
        this.properties = properties;
        this.legacy = false;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    public GameModes getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameModes gameMode) {
        this.gameMode = gameMode;
    }

    public TextComponent getDisplayName() {
        return (hasDisplayName() ? displayName : new TextComponent(name));
    }

    public boolean hasDisplayName() {
        return displayName != null;
    }

    public void setDisplayName(TextComponent displayName) {
        this.displayName = displayName;
    }

    public HashMap<PlayerProperties, PlayerProperty> getProperties() {
        return properties;
    }

    public PlayerProperty getProperty(PlayerProperties property) {
        return properties.get(property);
    }

    public boolean isLegacy() {
        return legacy;
    }
}
