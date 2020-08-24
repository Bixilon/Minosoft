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
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketPlayerInfo;

import java.util.HashMap;
import java.util.UUID;

// The holder for the data on <tab>
public class PlayerInfoBulk {
    // required fields
    final UUID uuid;
    final String name;
    final boolean legacy;
    final int ping;
    //optional fields
    final GameModes gameMode;
    final TextComponent displayName;
    final HashMap<PlayerProperties, PlayerProperty> properties;
    final PacketPlayerInfo.PlayerInfoActions action;

    /**
     * Legacy (1.7.10)
     *
     * @param name   Player name
     * @param ping   Ping in milliseconds
     * @param action
     */
    public PlayerInfoBulk(String name, int ping, PacketPlayerInfo.PlayerInfoActions action) {
        this.action = action;
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.ping = ping;
        gameMode = null;
        displayName = null;
        properties = null;
        this.legacy = true;
    }

    public PlayerInfoBulk(UUID uuid, String name, int ping, GameModes gameMode, TextComponent displayName, HashMap<PlayerProperties, PlayerProperty> properties, PacketPlayerInfo.PlayerInfoActions action) {
        this.uuid = uuid;
        this.name = name;
        this.ping = ping;
        this.gameMode = gameMode;
        this.displayName = displayName;
        this.properties = properties;
        this.action = action;
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

    public GameModes getGameMode() {
        return gameMode;
    }

    public boolean hasDisplayName() {
        return displayName != null;
    }

    public TextComponent getDisplayName() {
        return (hasDisplayName() ? displayName : new TextComponent(name));
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

    public PacketPlayerInfo.PlayerInfoActions getAction() {
        return action;
    }
}
