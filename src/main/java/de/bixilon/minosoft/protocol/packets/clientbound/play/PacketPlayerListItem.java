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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.GameModes;
import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.player.PlayerListItemBulk;
import de.bixilon.minosoft.game.datatypes.player.PlayerProperties;
import de.bixilon.minosoft.game.datatypes.player.PlayerProperty;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PacketPlayerListItem implements ClientboundPacket {
    final ArrayList<PlayerListItemBulk> playerList = new ArrayList<>();

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getProtocolId() < 17) { //ToDo: 19?
            String name = buffer.readString();
            int ping;
            if (buffer.getProtocolId() < 7) {
                ping = buffer.readShort();
            } else {
                ping = buffer.readVarInt();
            }
            PlayerListItemActions action = (buffer.readBoolean() ? PlayerListItemActions.UPDATE_LATENCY : PlayerListItemActions.REMOVE_PLAYER);
            playerList.add(new PlayerListItemBulk(name, ping, action));
            return true;
        }
        PlayerListItemActions action = PlayerListItemActions.byId(buffer.readVarInt());
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            UUID uuid = buffer.readUUID();
            PlayerListItemBulk listItemBulk;
            //UUID uuid, String name, int ping, GameMode gameMode, TextComponent displayName, HashMap< PlayerProperties, PlayerProperty > properties, PacketPlayerInfo.PlayerInfoAction action) {
            switch (action) {
                case ADD:
                    String name = buffer.readString();
                    int propertiesCount = buffer.readVarInt();
                    HashMap<PlayerProperties, PlayerProperty> playerProperties = new HashMap<>();
                    for (int p = 0; p < propertiesCount; p++) {
                        PlayerProperty property = new PlayerProperty(PlayerProperties.byName(buffer.readString()), buffer.readString(), (buffer.readBoolean() ? buffer.readString() : null));
                        playerProperties.put(property.getProperty(), property);
                    }
                    GameModes gameMode = GameModes.byId(buffer.readVarInt());
                    int ping = buffer.readVarInt();
                    TextComponent displayName = (buffer.readBoolean() ? buffer.readTextComponent() : null);
                    listItemBulk = new PlayerListItemBulk(uuid, name, ping, gameMode, displayName, playerProperties, action);
                    break;
                case UPDATE_GAMEMODE:
                    listItemBulk = new PlayerListItemBulk(uuid, null, 0, GameModes.byId(buffer.readVarInt()), null, null, action);
                    break;
                case UPDATE_LATENCY:
                    listItemBulk = new PlayerListItemBulk(uuid, null, buffer.readVarInt(), null, null, null, action);
                    break;
                case UPDATE_DISPLAY_NAME:
                    listItemBulk = new PlayerListItemBulk(uuid, null, 0, null, (buffer.readBoolean() ? buffer.readTextComponent() : null), null, action);
                    break;
                case REMOVE_PLAYER:
                    listItemBulk = new PlayerListItemBulk(uuid, null, 0, null, null, null, action);
                    break;
                default:
                    listItemBulk = null;
                    break;
            }
            playerList.add(listItemBulk);
        }
        return true;
    }

    @Override
    public void log() {
        for (PlayerListItemBulk property : playerList) {
            if (property.isLegacy()) {
                Log.game(String.format("[TAB] Player list item bulk (uuid=%s, name=%s, ping=%d)", property.getUUID(), property.getName(), property.getPing()));
            } else {
                Log.game(String.format("[TAB] Player list item bulk (uuid=%s, action=%s, name=%s, gameMode=%s, ping=%d, displayName=%s)", property.getUUID(), property.getAction(), property.getName(), property.getGameMode(), property.getPing(), property.getDisplayName()));
            }
        }
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public ArrayList<PlayerListItemBulk> getPlayerList() {
        return playerList;
    }

    public enum PlayerListItemActions {
        ADD,
        UPDATE_GAMEMODE,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME,
        REMOVE_PLAYER;

        public static PlayerListItemActions byId(int id) {
            return values()[id];
        }

        public int getId() {
            return ordinal();
        }
    }
}
