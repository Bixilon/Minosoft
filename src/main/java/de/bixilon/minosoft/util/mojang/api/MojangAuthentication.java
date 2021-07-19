/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.mojang.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.accounts.MojangAccount;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.HTTP;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.logging.LogMessageType;
import de.bixilon.minosoft.util.mojang.api.exceptions.AuthenticationException;
import de.bixilon.minosoft.util.mojang.api.exceptions.MojangJoinServerErrorException;
import de.bixilon.minosoft.util.mojang.api.exceptions.NoNetworkConnectionException;

import java.io.IOException;
import java.net.http.HttpResponse;

public final class MojangAuthentication {

    public static MojangAccount login(String username, String password) throws AuthenticationException, NoNetworkConnectionException {
        return login(Minosoft.config.getConfig().getAccount().getClientToken(), username, password);
    }

    public static MojangAccount login(String clientToken, String username, String password) throws NoNetworkConnectionException, AuthenticationException {
        JsonObject agent = new JsonObject();
        agent.addProperty("name", "Minecraft");
        agent.addProperty("version", 1);

        JsonObject payload = new JsonObject();
        payload.add("agent", agent);
        payload.addProperty("username", username);
        payload.addProperty("password", password);
        payload.addProperty("clientToken", clientToken);
        payload.addProperty("requestUser", true);

        HttpResponse<String> response;
        try {
            response = HTTP.postJson(ProtocolDefinition.MOJANG_URL_LOGIN, payload);
        } catch (IOException | InterruptedException e) {
            Log.printException(e, LogMessageType.OTHER);
            throw new NoNetworkConnectionException(e);
        }
        if (response == null) {
            Log.mojang(String.format("Failed to login with username %s", username));
            throw new NoNetworkConnectionException("Unknown error, check your Internet connection");
        }
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        if (response.statusCode() != 200) {
            Log.mojang(String.format("Failed to login with error code %d: %s", response.statusCode(), jsonResponse.get("errorMessage").getAsString()));
            throw new AuthenticationException(jsonResponse.get("errorMessage").getAsString());
        }
        // now it is okay
        return new MojangAccount(username, jsonResponse);
    }


    public static void joinServer(MojangAccount account, String serverId) throws NoNetworkConnectionException, MojangJoinServerErrorException {
        JsonObject payload = new JsonObject();
        payload.addProperty("accessToken", account.getAccessToken());
        payload.addProperty("selectedProfile", account.getUUID().toString().replace("-", ""));
        payload.addProperty("serverId", serverId);

        HttpResponse<String> response;
        try {
            response = HTTP.postJson(ProtocolDefinition.MOJANG_URL_JOIN, payload);
        } catch (IOException | InterruptedException e) {
            throw new NoNetworkConnectionException(e);
        }

        if (response == null) {
            Log.mojang(String.format("Failed to join server: %s", serverId));
            throw new MojangJoinServerErrorException();
        }
        if (response.statusCode() != 204) {
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            Log.mojang(String.format("Failed to join server with error code %d: %s", response.statusCode(), jsonResponse.has("errorMessage") ? jsonResponse.get("errorMessage").getAsString() : "null"));
            throw new MojangJoinServerErrorException(jsonResponse.get("errorMessage").getAsString());
        }
        // joined
        Log.mojang("Joined server successfully");
    }

    public static String refresh(String accessToken) throws NoNetworkConnectionException, AuthenticationException {
        return refresh(Minosoft.config.getConfig().getAccount().getClientToken(), accessToken);
    }

    public static String refresh(String clientToken, String accessToken) throws NoNetworkConnectionException, AuthenticationException {
        JsonObject payload = new JsonObject();
        payload.addProperty("accessToken", accessToken);
        payload.addProperty("clientToken", clientToken);

        HttpResponse<String> response;
        try {
            response = HTTP.postJson(ProtocolDefinition.MOJANG_URL_REFRESH, payload);
        } catch (IOException | InterruptedException e) {
            Log.mojang(String.format("Could not connect to mojang server: %s", e.getCause().toString()));
            throw new NoNetworkConnectionException(e);
        }
        if (response == null) {
            Log.mojang("Failed to refresh session");
            throw new NoNetworkConnectionException();
        }
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        if (response.statusCode() != 200) {
            Log.mojang(String.format("Failed to refresh session with error code %d: %s", response.statusCode(), jsonResponse.get("errorMessage").getAsString()));
            throw new AuthenticationException(jsonResponse.get("errorMessage").getAsString());
        }
        // now it is okay
        Log.mojang("Refreshed 1 session token");
        return jsonResponse.get("accessToken").getAsString();
    }
}
