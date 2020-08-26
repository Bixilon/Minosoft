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

package de.bixilon.minosoft;

import com.google.gson.JsonObject;
import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.gui.main.GUITools;
import javafx.scene.image.Image;

public class ServerListPing {
    final JsonObject raw;

    public ServerListPing(JsonObject json) {
        this.raw = json;
    }

    public int getProtocolNumber() {
        return raw.getAsJsonObject("version").get("protocol").getAsInt();
    }

    public String getServerBrand() {
        return raw.getAsJsonObject("version").get("name").getAsString();
    }

    public int getPlayerOnline() {
        return raw.getAsJsonObject("players").get("online").getAsInt();
    }

    public int getMaxPlayers() {
        return raw.getAsJsonObject("players").get("max").getAsInt();
    }

    public String getBase64EncodedFavicon() {
        if (raw.has("favicon")) {
            return raw.get("favicon").getAsString().replace("data:image/png;base64,", "");
        }
        return null;
    }

    public Image getFavicon() {
        return GUITools.getImageFromBase64(getBase64EncodedFavicon());
    }

    public TextComponent getMotd() {
        try {
            return new TextComponent(raw.getAsJsonObject("description"));
        } catch (Exception ignored) {
        }
        return new TextComponent(raw.get("description").getAsString());
    }

    public JsonObject getRaw() {
        return this.raw;
    }
}
