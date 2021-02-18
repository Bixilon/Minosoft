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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MojangAccount extends Account {
    protected final String id;
    protected final String email;
    protected String accessToken;
    protected RefreshStates lastRefreshStatus;
    protected boolean needsRefresh = true;

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

    public static MojangAccount deserialize(Map<String, Object> json) {
        return new MojangAccount((String) json.get("accessToken"), (String) json.get("id"), Util.getUUIDFromString((String) json.get("uuid")), (String) json.get("username"), (String) json.get("email"));
    }

    public Map<String, Object> serialize() {
        Map<String, Object> json = new HashMap<>();
        json.put("id", this.id);
        json.put("accessToken", this.accessToken);
        json.put("uuid", getUUID().toString());
        json.put("username", getUsername());
        json.put("email", this.email);
        json.put("type", "mojang");
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
        Minosoft.getConfig().getConfig().getAccount().getEntries().remove(this.getId());
        Minosoft.getConfig().saveToFile();
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
