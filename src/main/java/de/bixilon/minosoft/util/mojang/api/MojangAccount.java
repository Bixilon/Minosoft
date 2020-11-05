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

package de.bixilon.minosoft.util.mojang.api;

import com.google.gson.JsonObject;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.util.Util;

import java.util.UUID;

public class MojangAccount {

    final String userId;
    final UUID uuid;
    final String playerName;
    final String mojangUserName;
    String accessToken;
    RefreshStates lastRefreshStatus;

    public MojangAccount(String username, JsonObject json) {
        this.accessToken = json.get("accessToken").getAsString();
        JsonObject profile = json.get("selectedProfile").getAsJsonObject();
        this.uuid = Util.getUUIDFromString(profile.get("id").getAsString());
        this.playerName = profile.get("name").getAsString();

        JsonObject mojang = json.get("user").getAsJsonObject();
        this.userId = mojang.get("id").getAsString();
        this.mojangUserName = username;
    }

    public MojangAccount(String accessToken, String userId, UUID uuid, String playerName, String mojangUserName) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.uuid = uuid;
        this.playerName = playerName;
        this.mojangUserName = mojangUserName;
    }

    public static MojangAccount deserialize(JsonObject json) {
        return new MojangAccount(json.get("accessToken").getAsString(), json.get("userId").getAsString(), Util.getUUIDFromString(json.get("uuid").getAsString()), json.get("playerName").getAsString(), json.get("userName").getAsString());
    }

    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("accessToken", accessToken);
        json.addProperty("userId", userId);
        json.addProperty("uuid", uuid.toString());
        json.addProperty("playerName", playerName);
        json.addProperty("userName", mojangUserName);
        return json;
    }

    public void join(String serverId) {
        MojangAuthentication.joinServer(this, serverId);
    }

    public RefreshStates refreshToken() {
        if (lastRefreshStatus != null) {
            return lastRefreshStatus;
        }
        String accessToken = MojangAuthentication.refresh(this.accessToken);
        if (accessToken == null) {
            lastRefreshStatus = RefreshStates.FAILED;
            return lastRefreshStatus;
        }
        if (accessToken.isBlank()) {
            lastRefreshStatus = RefreshStates.ERROR;
            return lastRefreshStatus;
        }
        this.accessToken = accessToken;
        lastRefreshStatus = RefreshStates.SUCCESSFUL;
        return lastRefreshStatus;
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

    public void saveToConfig() {
        Minosoft.getConfig().putMojangAccount(this);
        Minosoft.getConfig().saveToFile();
    }

    public void delete() {
        Minosoft.getAccountList().remove(this.getUserId());
        Minosoft.getConfig().removeAccount(this);
        Minosoft.getConfig().saveToFile();
    }

    @Override
    public String toString() {
        return getUserId();
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        MojangAccount account = (MojangAccount) obj;
        return account.getUserId().equals(getUserId());
    }

    public enum RefreshStates {
        SUCCESSFUL,
        ERROR, // account not valid anymore
        FAILED // error occurred while checking -> Unknown state
    }

}
