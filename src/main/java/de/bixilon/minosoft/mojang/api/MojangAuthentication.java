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
import de.bixilon.minosoft.config.GameConfiguration;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.HTTP;
import org.json.JSONObject;

import java.net.http.HttpResponse;

public class MojangAuthentication {

    public static MojangAccount login(String clientToken, String username, String password) {
        JSONObject payload = new JSONObject();
        payload.put("agent", new JSONObject().put("name", "Minecraft").put("version", 1));
        payload.put("username", username);
        payload.put("password", password);
        payload.put("clientToken", clientToken);
        payload.put("requestUser", true);

        HttpResponse<String> response = HTTP.postJson(MojangURLs.LOGIN.getUrl(), payload);
        if (response == null) {
            Log.mojang(String.format("Failed to login with username %s", username));
            return null;
        }
        JSONObject jsonResponse = new JSONObject(response.body());
        if (response.statusCode() != 200) {
            Log.mojang(String.format("Failed to login with error code %d: %s", response.statusCode(), jsonResponse.getString("errorMessage")));
            return null;
        }
        // now it is okay
        return new MojangAccount(jsonResponse);
    }

    public static MojangAccount login(String username, String password) {
        return login(Minosoft.getConfig().getString(GameConfiguration.CLIENT_TOKEN), username, password);

    }

    public static void joinServer(MojangAccount account, String serverId) {
        if (Config.skipAuthentication) {
            return;
        }

        JSONObject payload = new JSONObject();
        payload.put("accessToken", account.getAccessToken());
        payload.put("selectedProfile", account.getUUID().toString().replace("-", ""));
        payload.put("serverId", serverId);

        HttpResponse<String> response = HTTP.postJson(MojangURLs.JOIN.toString(), payload);

        if (response == null) {
            Log.mojang(String.format("Failed to join server: %s", serverId));
            return;
        }
        if (response.statusCode() != 204) {
            JSONObject jsonResponse = new JSONObject(response.body());
            Log.mojang(String.format("Failed to join server with error code %d: %s", response.statusCode(), jsonResponse.has("errorMessage") ? jsonResponse.getString("errorMessage") : "null"));
            return;
        }
        // joined
        Log.mojang("Joined server successfully");
    }

    public static String refresh(String clientToken, String accessToken) {
        if (Config.skipAuthentication) {
            return clientToken;
        }
        JSONObject payload = new JSONObject();
        payload.put("accessToken", accessToken);
        payload.put("clientToken", clientToken);

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
        JSONObject jsonResponse = new JSONObject(response.body());
        if (response.statusCode() != 200) {
            Log.mojang(String.format("Failed to refresh session with error code %d: %s", response.statusCode(), jsonResponse.getString("errorMessage")));
            return null;
        }
        // now it is okay
        return jsonResponse.getString("accessToken");
    }

    public static String refresh(String accessToken) {
        return refresh(Minosoft.getConfig().getString(GameConfiguration.CLIENT_TOKEN), accessToken);
    }
}
