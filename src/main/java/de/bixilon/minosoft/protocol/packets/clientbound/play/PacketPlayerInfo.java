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

import de.bixilon.minosoft.game.datatypes.GameMode;
import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.player.PlayerInfoBulk;
import de.bixilon.minosoft.game.datatypes.player.PlayerProperties;
import de.bixilon.minosoft.game.datatypes.player.PlayerProperty;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PacketPlayerInfo implements ClientboundPacket {
    final ArrayList<PlayerInfoBulk> infos = new ArrayList<>();


    @Override
    public boolean read(InByteBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                infos.add(new PlayerInfoBulk(buffer.readString(), buffer.readShort(), (buffer.readBoolean() ? PlayerInfoAction.UPDATE_LATENCY : PlayerInfoAction.REMOVE_PLAYER)));
                return true;
            default:
                PlayerInfoAction action = PlayerInfoAction.byId(buffer.readVarInt());
                int count = buffer.readVarInt();
                for (int i = 0; i < count; i++) {
                    UUID uuid = buffer.readUUID();
                    PlayerInfoBulk infoBulk;
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
                            GameMode gameMode = GameMode.byId(buffer.readVarInt());
                            int ping = buffer.readVarInt();
                            TextComponent displayName = (buffer.readBoolean() ? buffer.readTextComponent() : null);
                            infoBulk = new PlayerInfoBulk(uuid, name, ping, gameMode, displayName, playerProperties, action);
                            break;
                        case UPDATE_GAMEMODE:
                            infoBulk = new PlayerInfoBulk(uuid, null, 0, GameMode.byId(buffer.readVarInt()), null, null, action);
                            break;
                        case UPDATE_LATENCY:
                            infoBulk = new PlayerInfoBulk(uuid, null, buffer.readVarInt(), null, null, null, action);
                            break;
                        case UPDATE_DISPLAY_NAME:
                            infoBulk = new PlayerInfoBulk(uuid, null, 0, null, (buffer.readBoolean() ? buffer.readTextComponent() : null), null, action);
                            break;
                        case REMOVE_PLAYER:
                            infoBulk = new PlayerInfoBulk(uuid, null, 0, null, null, null, action);
                            break;
                        default:
                            infoBulk = null;
                            break;
                    }
                    infos.add(infoBulk);
                }
                return true;
        }
    }

    @Override
    public void log() {
        for (PlayerInfoBulk property : infos) {
            if (property.isLegacy()) {
                Log.game(String.format("[TAB] Player info bulk (uuid=%s, name=%s, ping=%d)", property.getUUID(), property.getName(), property.getPing()));
            } else {
                Log.game(String.format("[TAB] Player info bulk (uuid=%s, action=%s, name=%s, gameMode=%s, ping=%d, displayName=%s)", property.getUUID(), property.getAction(), property.getName(), property.getGameMode(), property.getPing(), property.getDisplayName()));
            }
        }
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public ArrayList<PlayerInfoBulk> getInfos() {
        return infos;
    }

    public enum PlayerInfoAction {
        ADD(0),
        UPDATE_GAMEMODE(1),
        UPDATE_LATENCY(2),
        UPDATE_DISPLAY_NAME(3),
        REMOVE_PLAYER(4);

        final int id;

        PlayerInfoAction(int id) {
            this.id = id;
        }

        public static PlayerInfoAction byId(int id) {
            for (PlayerInfoAction a : values()) {
                if (a.getId() == id) {
                    return a;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
