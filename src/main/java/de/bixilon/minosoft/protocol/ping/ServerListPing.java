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
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.data.text.BaseComponent;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.logging.Log;

import java.util.Base64;

public class ServerListPing {
    private final ServerModInfo serverModInfo;
    private final int protocolId;
    private final int playersOnline;
    private final int maxPlayers;
    private final ChatComponent motd;
    private final String serverBrand;
    byte[] favicon;

    public ServerListPing(Version version, JsonObject json) {
        int protocolId = json.getAsJsonObject("version").get("protocol").getAsInt();
        if (protocolId == ProtocolDefinition.QUERY_PROTOCOL_VERSION_ID) {
            // Server did not send us a version, trying 1.8
            this.protocolId = ProtocolDefinition.FALLBACK_PROTOCOL_VERSION_ID;
            Log.warn(String.format("Server sent us an illegal version id (protocolId=%d). Using 1.8.9.", protocolId));
        } else {
            this.protocolId = protocolId;
        }
        this.playersOnline = json.getAsJsonObject("players").get("online").getAsInt();
        this.maxPlayers = json.getAsJsonObject("players").get("max").getAsInt();
        if (json.has("favicon")) {
            this.favicon = Base64.getDecoder().decode(json.get("favicon").getAsString().replace("data:image/png;base64,", "").replace("\n", ""));
        }

        if (json.get("description").isJsonPrimitive()) {
            this.motd = ChatComponent.valueOf(json.get("description").getAsString());
        } else {
            this.motd = new BaseComponent(version, json.getAsJsonObject("description"));
        }
        this.serverBrand = json.getAsJsonObject("version").get("name").getAsString();

        if (json.has("modinfo") && json.getAsJsonObject("modinfo").has("type") && json.getAsJsonObject("modinfo").get("type").getAsString().equals("FML")) {
            this.serverModInfo = new ForgeModInfo(json.getAsJsonObject("modinfo"));
        } else {
            this.serverModInfo = new VanillaModInfo();
        }
    }

    public int getProtocolId() {
        return this.protocolId;
    }

    public int getPlayerOnline() {
        return this.playersOnline;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public byte[] getFavicon() {
        return this.favicon;
    }

    public ChatComponent getMotd() {
        return this.motd;
    }

    public String getServerBrand() {
        return this.serverBrand;
    }

    public <T extends ServerModInfo> T getServerModInfo() {
        return (T) this.serverModInfo;
    }
}
