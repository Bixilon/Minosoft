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

package de.bixilon.minosoft.data.accounts;

import com.google.gson.JsonObject;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.mojang.api.MojangAuthentication;
import de.bixilon.minosoft.util.mojang.api.exceptions.AuthenticationException;
import de.bixilon.minosoft.util.mojang.api.exceptions.MojangJoinServerErrorException;
import de.bixilon.minosoft.util.mojang.api.exceptions.NoNetworkConnectionException;

import java.util.UUID;

public class MojangAccount extends Account {
    private final String id;
    private final String email;
    private String accessToken;
    private RefreshStates lastRefreshStatus;
    private boolean needsRefresh = true;

    public MojangAccount(String username, JsonObject json) {
        super(json.getAsJsonObject("selectedProfile").get("name").getAsString(), Util.getUUIDFromString(json.getAsJsonObject("selectedProfile").get("id").getAsString()));
        this.accessToken = json.get("accessToken").getAsString();
        this.id = json.getAsJsonObject("user").get("id").getAsString();
        this.email = username;
    }

    public MojangAccount(String accessToken, String id, UUID uuid, String username, String email) {
        super(username, uuid);
        this.accessToken = accessToken;
        this.id = id;
        this.email = email;
    }

    public static MojangAccount deserialize(JsonObject json) {
        return new MojangAccount(json.get("accessToken").getAsString(), json.get("id").getAsString(), Util.getUUIDFromString(json.get("uuid").getAsString()), json.get("username").getAsString(), json.get("email").getAsString());
    }

    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("id", this.id);
        json.addProperty("accessToken", this.accessToken);
        json.addProperty("uuid", getUUID().toString());
        json.addProperty("username", getUsername());
        json.addProperty("email", this.email);
        json.addProperty("type", "mojang");
        return json;
    }

    public void join(String serverId) throws MojangJoinServerErrorException, NoNetworkConnectionException {
        MojangAuthentication.joinServer(this, serverId);
    }

    @Override
    public boolean select() {
        if (this.needsRefresh) {
            return refreshToken() != RefreshStates.ERROR;
        }
        return true;
    }

    @Override
    public void logout() {
    }

    @Override
    public String getId() {
        return this.id;
    }

    public RefreshStates refreshToken() {
        try {
            this.accessToken = MojangAuthentication.refresh(this.accessToken);
            this.lastRefreshStatus = RefreshStates.SUCCESSFUL;
        } catch (NoNetworkConnectionException e) {
            e.printStackTrace();
            this.lastRefreshStatus = RefreshStates.FAILED;
        } catch (AuthenticationException e) {
            e.printStackTrace();
            this.lastRefreshStatus = RefreshStates.ERROR;
        }
        return this.lastRefreshStatus;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        this.needsRefresh = false;
    }

    public String getEmail() {
        return this.email;
    }

    public void saveToConfig() {
        Minosoft.getConfig().putAccount(this);
        Minosoft.getConfig().saveToFile();
    }

    public void delete() {
        Minosoft.getConfig().removeAccount(this);
        Minosoft.getConfig().saveToFile();
    }

    @Override
    public String toString() {
        return getId();
    }

    public boolean needsRefresh() {
        return this.needsRefresh;
    }

    public void setNeedRefresh(boolean needsRefresh) {
        this.needsRefresh = needsRefresh;
    }

    public enum RefreshStates {
        SUCCESSFUL,
        ERROR, // account not valid anymore
        FAILED // error occurred while checking -> Unknown state
    }
}
