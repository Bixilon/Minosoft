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

import de.bixilon.minosoft.game.datatypes.TextComponent;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerListPing {
    final JSONObject raw;

    public ServerListPing(JSONObject json) {
        this.raw = json;
    }

    public int getProtocolNumber() {
        return raw.getJSONObject("version").getInt("protocol");
    }

    public String getServerBrand() {
        return raw.getJSONObject("version").getString("name");
    }

    public int getPlayerOnline() {
        return raw.getJSONObject("players").getInt("online");
    }

    public int getMaxPlayers() {
        return raw.getJSONObject("players").getInt("max");
    }

    public String getBase64EncodedFavicon() {
        return raw.getString("favicon");
    }

    public TextComponent getMotd() {
        try {
            return new TextComponent(raw.getJSONObject("description"));
        } catch (JSONException ignored) {
        }
        return new TextComponent(raw.getString("description"));
    }

    public JSONObject getRaw() {
        return this.raw;
    }
}
