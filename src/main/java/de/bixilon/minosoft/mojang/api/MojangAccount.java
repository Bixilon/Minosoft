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

package de.bixilon.minosoft.mojang.api;

import de.bixilon.minosoft.Config;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.util.Util;
import org.json.JSONObject;

import java.util.UUID;

public class MojangAccount {

    String accessToken;
    String userId;

    UUID uuid;
    String playerName;

    String mojangUserName;

    public MojangAccount(JSONObject json) {
        this.accessToken = json.getString("accessToken");
        JSONObject profile = json.getJSONObject("selectedProfile");
        this.uuid = Util.formatUUID(profile.getString("id"));
        this.playerName = profile.getString("name");

        JSONObject mojang = json.getJSONObject("user");
        this.userId = mojang.getString("id");
        this.mojangUserName = mojang.getString("username");
    }

    public MojangAccount(String accessToken, String userId, UUID uuid, String playerName, String mojangUserName) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.uuid = uuid;
        this.playerName = playerName;
        this.mojangUserName = mojangUserName;
    }

    public void join(String serverId) {
        MojangAuthentication.joinServer(this, serverId);
    }

    public boolean refreshToken() {
        String accessToken = MojangAuthentication.refresh(this.accessToken);
        if (accessToken == null) {
            return false;
        }
        this.accessToken = accessToken;
        return true;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getMojangUserName() {
        return mojangUserName;
    }

    public String getUserId() {
        return userId;
    }

    public void saveToConfig() {
        Minosoft.getConfig().putMojangAccount(this);
        Minosoft.getConfig().saveToFile(Config.configFileName);
    }
}
