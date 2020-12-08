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

package de.bixilon.minosoft.protocol.ping;

import com.google.gson.JsonObject;
import de.bixilon.minosoft.data.text.BaseComponent;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.Base64;

public class ServerListPing {
    final ServerModInfo serverModInfo;
    final int protocolId;
    final int playersOnline;
    final int maxPlayers;
    final ChatComponent motd;
    final String serverBrand;
    byte[] favicon;

    public ServerListPing(JsonObject json) {
        int protocolId = json.getAsJsonObject("version").get("protocol").getAsInt();
        if (protocolId == -1) {
            // Server did not send us a version, trying 1.8
            this.protocolId = ProtocolDefinition.FALLBACK_PROTOCOL_VERSION_ID;
            Log.warn(String.format("Server sent us an illegal version id (protocolId=%d). Using 1.8.9.", protocolId));
        } else {
            this.protocolId = protocolId;
        }
        playersOnline = json.getAsJsonObject("players").get("online").getAsInt();
        maxPlayers = json.getAsJsonObject("players").get("max").getAsInt();
        if (json.has("favicon")) {
            favicon = Base64.getDecoder().decode(json.get("favicon").getAsString().replace("data:image/png;base64,", "").replace("\n", ""));
        }

        if (json.get("description").isJsonPrimitive()) {
            motd = ChatComponent.valueOf(json.get("description").getAsString());
        } else {
            motd = new BaseComponent(json.getAsJsonObject("description"));
        }
        serverBrand = json.getAsJsonObject("version").get("name").getAsString();

        if (json.has("modinfo") && json.getAsJsonObject("modinfo").has("type") && json.getAsJsonObject("modinfo").get("type").getAsString().equals("FML")) {
            serverModInfo = new ForgeModInfo(json.getAsJsonObject("modinfo"));
        } else {
            serverModInfo = new VanillaModInfo();
        }
    }

    public int getProtocolId() {
        return protocolId;
    }

    public int getPlayerOnline() {
        return playersOnline;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public byte[] getFavicon() {
        return favicon;
    }

    public ChatComponent getMotd() {
        return motd;
    }

    public String getServerBrand() {
        return serverBrand;
    }

    public ServerModInfo getServerModInfo() {
        return serverModInfo;
    }
}
