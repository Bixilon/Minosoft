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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.GameModes;
import de.bixilon.minosoft.data.player.PlayerListItem;
import de.bixilon.minosoft.data.player.PlayerListItemBulk;
import de.bixilon.minosoft.data.player.PlayerProperties;
import de.bixilon.minosoft.data.player.PlayerProperty;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.modding.event.events.PlayerListItemChangeEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W19A;

public class PacketPlayerListItem extends ClientboundPacket {
    private final ArrayList<PlayerListItemBulk> playerList = new ArrayList<>();

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < V_14W19A) { // ToDo: 19?
            String name = buffer.readString();
            int ping;
            if (buffer.getVersionId() < V_14W04A) {
                ping = buffer.readUnsignedShort();
            } else {
                ping = buffer.readVarInt();
            }
            PlayerListItemActions action = (buffer.readBoolean() ? PlayerListItemActions.UPDATE_LATENCY : PlayerListItemActions.REMOVE_PLAYER);
            this.playerList.add(new PlayerListItemBulk(name, ping, action));
            return true;
        }
        PlayerListItemActions action = PlayerListItemActions.byId(buffer.readVarInt());
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            UUID uuid = buffer.readUUID();
            PlayerListItemBulk listItemBulk;
            // UUID uuid, String name, int ping, GameMode gameMode, TextComponent displayName, HashMap< PlayerProperties, PlayerProperty > properties, PacketPlayerInfo.PlayerInfoAction action) {
            switch (action) {
                case ADD -> {
                    String name = buffer.readString();
                    int propertiesCount = buffer.readVarInt();
                    HashMap<PlayerProperties, PlayerProperty> playerProperties = new HashMap<>();
                    for (int p = 0; p < propertiesCount; p++) {
                        PlayerProperty property = new PlayerProperty(PlayerProperties.byName(buffer.readString()), buffer.readString(), (buffer.readBoolean() ? buffer.readString() : null));
                        playerProperties.put(property.getProperty(), property);
                    }
                    GameModes gameMode = GameModes.byId(buffer.readVarInt());
                    int ping = buffer.readVarInt();
                    ChatComponent displayName = (buffer.readBoolean() ? buffer.readChatComponent() : null);
                    listItemBulk = new PlayerListItemBulk(uuid, name, ping, gameMode, displayName, playerProperties, action);
                }
                case UPDATE_GAMEMODE -> listItemBulk = new PlayerListItemBulk(uuid, null, 0, GameModes.byId(buffer.readVarInt()), null, null, action);
                case UPDATE_LATENCY -> listItemBulk = new PlayerListItemBulk(uuid, null, buffer.readVarInt(), null, null, null, action);
                case UPDATE_DISPLAY_NAME -> listItemBulk = new PlayerListItemBulk(uuid, null, 0, null, (buffer.readBoolean() ? buffer.readChatComponent() : null), null, action);
                case REMOVE_PLAYER -> listItemBulk = new PlayerListItemBulk(uuid, null, 0, null, null, null, action);
                default -> listItemBulk = null;
            }
            this.playerList.add(listItemBulk);
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        if (connection.fireEvent(new PlayerListItemChangeEvent(connection, this))) {
            return;
        }
        for (PlayerListItemBulk bulk : getPlayerList()) {
            PlayerListItem item = connection.getPlayer().getPlayerList().get(bulk.getUUID());
            if (bulk.getAction() != PlayerListItemActions.ADD && item == null && !bulk.isLegacy()) {
                // Aaaaah. Fuck this shit. The server sends us bullshit!
                continue;
            }
            switch (bulk.getAction()) {
                case ADD -> connection.getPlayer().getPlayerList().put(bulk.getUUID(), new PlayerListItem(bulk.getUUID(), bulk.getName(), bulk.getPing(), bulk.getGameMode(), bulk.getDisplayName(), bulk.getProperties()));
                case UPDATE_LATENCY -> {
                    if (bulk.isLegacy()) {
                        // add or update
                        if (item == null) {
                            // create
                            UUID uuid = UUID.randomUUID();
                            connection.getPlayer().getPlayerList().put(uuid, new PlayerListItem(uuid, bulk.getName(), bulk.getPing()));
                        } else {
                            // update ping
                            item.setPing(bulk.getPing());
                        }
                        continue;
                    }
                    connection.getPlayer().getPlayerList().get(bulk.getUUID()).setPing(bulk.getPing());
                }
                case REMOVE_PLAYER -> {
                    if (bulk.isLegacy()) {
                        if (item == null) {
                            // not initialized yet
                            continue;
                        }
                        connection.getPlayer().getPlayerList().remove(connection.getPlayer().getPlayerListItem(bulk.getName()).getUUID());
                        continue;
                    }
                    connection.getPlayer().getPlayerList().remove(bulk.getUUID());
                }
                case UPDATE_GAMEMODE -> item.setGameMode(bulk.getGameMode());
                case UPDATE_DISPLAY_NAME -> item.setDisplayName(bulk.getDisplayName());
            }
        }
    }

    @Override
    public void log() {
        for (PlayerListItemBulk property : this.playerList) {
            Log.protocol(String.format("[IN] Received player list item bulk (%s)", property));
        }
    }

    public ArrayList<PlayerListItemBulk> getPlayerList() {
        return this.playerList;
    }

    public enum PlayerListItemActions {
        ADD,
        UPDATE_GAMEMODE,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME,
        REMOVE_PLAYER;

        private static final PlayerListItemActions[] PLAYER_LIST_ITEM_ACTIONS = values();

        public static PlayerListItemActions byId(int id) {
            return PLAYER_LIST_ITEM_ACTIONS[id];
        }
    }
}
