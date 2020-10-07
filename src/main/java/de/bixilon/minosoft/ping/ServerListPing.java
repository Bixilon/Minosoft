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

package de.bixilon.minosoft.ping;

import com.google.gson.JsonObject;
import de.bixilon.minosoft.game.datatypes.text.BaseComponent;
import de.bixilon.minosoft.game.datatypes.text.ChatComponent;
import de.bixilon.minosoft.gui.main.GUITools;
import javafx.scene.image.Image;

public class ServerListPing {
    final ServerModInfo serverModInfo;
    final int protocolId;
    final int playersOnline;
    final int maxPlayers;
    String base64Favicon;
    Image favicon;
    final ChatComponent motd;
    final String serverBrand;

    public ServerListPing(JsonObject json) {
        protocolId = json.getAsJsonObject("version").get("protocol").getAsInt();
        playersOnline = json.getAsJsonObject("players").get("online").getAsInt();
        maxPlayers = json.getAsJsonObject("players").get("max").getAsInt();
        if (json.has("favicon")) {
            base64Favicon = json.get("favicon").getAsString().replace("data:image/png;base64,", "").replace("\n", "");
            favicon = GUITools.getImageFromBase64(base64Favicon);
        }

        if (json.get("description").isJsonPrimitive()) {
            motd = ChatComponent.fromString(json.get("description").getAsString());
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

    public String getBase64EncodedFavicon() {
        return base64Favicon;
    }

    public Image getFavicon() {
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
