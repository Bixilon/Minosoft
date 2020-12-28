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

package de.bixilon.minosoft.data.player;

import de.bixilon.minosoft.data.GameModes;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketPlayerListItem;

import java.util.HashMap;
import java.util.UUID;

// The holder for the data on <tab>
public class PlayerListItemBulk {
    // required fieldsp
    private final UUID uuid;
    private final String name;
    private final boolean legacy;
    private final int ping;
    // optional fields
    private final GameModes gameMode;
    private final ChatComponent displayName;
    private final HashMap<PlayerProperties, PlayerProperty> properties;
    private final PacketPlayerListItem.PlayerListItemActions action;

    public PlayerListItemBulk(String name, int ping, PacketPlayerListItem.PlayerListItemActions action) {
        this.action = action;
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.ping = ping;
        this.gameMode = null;
        this.displayName = null;
        this.properties = null;
        this.legacy = true;
    }

    public PlayerListItemBulk(UUID uuid, String name, int ping, GameModes gameMode, ChatComponent displayName, HashMap<PlayerProperties, PlayerProperty> properties, PacketPlayerListItem.PlayerListItemActions action) {
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
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public int getPing() {
        return this.ping;
    }

    public GameModes getGameMode() {
        return this.gameMode;
    }

    public ChatComponent getDisplayName() {
        return (hasDisplayName() ? this.displayName : ChatComponent.valueOf(this.name));
    }

    public boolean hasDisplayName() {
        return this.displayName != null;
    }

    public HashMap<PlayerProperties, PlayerProperty> getProperties() {
        return this.properties;
    }

    public PlayerProperty getProperty(PlayerProperties property) {
        return this.properties.get(property);
    }

    public boolean isLegacy() {
        return this.legacy;
    }

    public PacketPlayerListItem.PlayerListItemActions getAction() {
        return this.action;
    }

    @Override
    public String toString() {
        return String.format("uuid=%s, action=%s, name=%s, gameMode=%s, ping=%d, displayName=%s", getUUID(), getAction(), getName(), getGameMode(), getPing(), getDisplayName());
    }
}
