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

package de.bixilon.minosoft.util.mojang.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bixilon.minosoft.Config;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.GameConfiguration;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.HTTP;

import java.net.http.HttpResponse;

public final class MojangAuthentication {

    public static MojangAccountAuthenticationAttempt login(String username, String password) {
        return login(Minosoft.getConfig().getString(GameConfiguration.CLIENT_TOKEN), username, password);
    }

    public static MojangAccountAuthenticationAttempt login(String clientToken, String username, String password) {
        JsonObject agent = new JsonObject();
        agent.addProperty("name", "Minecraft");
        agent.addProperty("version", 1);

        JsonObject payload = new JsonObject();
        payload.add("agent", agent);
        payload.addProperty("username", username);
        payload.addProperty("password", password);
        payload.addProperty("clientToken", clientToken);
        payload.addProperty("requestUser", true);

        HttpResponse<String> response = HTTP.postJson(MojangURLs.LOGIN.getUrl(), payload);
        if (response == null) {
            Log.mojang(String.format("Failed to login with username %s", username));
            return new MojangAccountAuthenticationAttempt("Unknown error, check your Internet connection");
        }
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        if (response.statusCode() != 200) {
            Log.mojang(String.format("Failed to login with error code %d: %s", response.statusCode(), jsonResponse.get("errorMessage").getAsString()));
            return new MojangAccountAuthenticationAttempt(jsonResponse.get("errorMessage").getAsString());
        }
        // now it is okay
        return new MojangAccountAuthenticationAttempt(new MojangAccount(username, jsonResponse));
    }

    public static void joinServer(MojangAccount account, String serverId) {
        if (Config.skipAuthentication) {
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("accessToken", account.getAccessToken());
        payload.addProperty("selectedProfile", account.getUUID().toString().replace("-", ""));
        payload.addProperty("serverId", serverId);

        HttpResponse<String> response = HTTP.postJson(MojangURLs.JOIN.toString(), payload);

        if (response == null) {
            Log.mojang(String.format("Failed to join server: %s", serverId));
            return;
        }
        if (response.statusCode() != 204) {
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            Log.mojang(String.format("Failed to join server with error code %d: %s", response.statusCode(), jsonResponse.has("errorMessage") ? jsonResponse.get("errorMessage").getAsString() : "null"));
            return;
        }
        // joined
        Log.mojang("Joined server successfully");
    }

    public static String refresh(String accessToken) {
        return refresh(Minosoft.getConfig().getString(GameConfiguration.CLIENT_TOKEN), accessToken);
    }

    public static String refresh(String clientToken, String accessToken) {
        if (Config.skipAuthentication) {
            return clientToken;
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("accessToken", accessToken);
        payload.addProperty("clientToken", clientToken);

        HttpResponse<String> response;
        try {
            response = HTTP.postJson(MojangURLs.REFRESH.getUrl(), payload);
        } catch (Exception e) {
            Log.mojang(String.format("Could not connect to mojang server: %s", e.getCause().toString()));
            return null;
        }
        if (response == null) {
            Log.mojang("Failed to refresh session");
            return null;
        }
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        if (response.statusCode() != 200) {
            Log.mojang(String.format("Failed to refresh session with error code %d: %s", response.statusCode(), jsonResponse.get("errorMessage").getAsString()));
            return "";
        }
        // now it is okay
        Log.mojang("Refreshed 1 session token");
        return jsonResponse.get("accessToken").getAsString();
    }
}
