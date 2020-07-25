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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.HTTP;

import java.net.http.HttpResponse;
import java.util.HashMap;

public class MojangStatus {
    public static HashMap<Services, ServiceStatus> getStatus() {
        HttpResponse<String> response = HTTP.get(MojangURLs.STATUS.getUrl());
        if (response == null) {
            Log.mojang("Failed to fetch Status");
            return getUnknownStatusMap();
        }
        if (response.statusCode() != 200) {
            Log.mojang(String.format("Failed to fetch Status with error code %d", response.statusCode()));
            return getUnknownStatusMap();
        }
        // now it is hopefully okay
        HashMap<Services, ServiceStatus> ret = new HashMap<>();
        try {
            JsonArray json = JsonParser.parseString(response.body()).getAsJsonArray();
            for (int i = 0; i < json.size(); i++) {
                JsonObject innerJson = json.get(i).getAsJsonObject();
                for (String key : innerJson.keySet()) {
                    Services service = Services.byKey(key);
                    ret.put(service, ServiceStatus.byKey(innerJson.get(key).getAsString()));
                }
            }
            if (ret.size() != Services.values().length) {
                // new service or old one removed, technically an error
                return ret;
            }
            return ret;

        } catch (NullPointerException | JsonParseException e) {
            e.printStackTrace();
            return getUnknownStatusMap();
        }
    }

    static HashMap<Services, ServiceStatus> getUnknownStatusMap() {
        HashMap<Services, ServiceStatus> ret = new HashMap<>();
        for (Services service : Services.values()) {
            ret.put(service, ServiceStatus.UNKNOWN);
        }
        return ret;
    }

    enum Services {
        MINECRAFT_NET("minecraft.net"),
        ACCOUNT("account.mojang.com"),
        AUTH("auth.mojang.com"),
        AUTHENTICATION_SERVER("authserver.mojang.com"),
        SESSION_SERVER("sessionserver.mojang.com"),
        API("api.mojang.com"),
        TEXTURES("textures.minecraft.net"),
        MOJANG_COM("mojang.com");

        final String key;

        Services(String key) {
            this.key = key;
        }

        public static Services byKey(String key) {
            for (Services s : values()) {
                if (s.getKey().equals(key)) {
                    return s;
                }
            }
            return null;
        }

        public String getKey() {
            return key;
        }
    }

    enum ServiceStatus {
        GREEN,
        YELLOW,
        RED,
        UNKNOWN;

        public static ServiceStatus byKey(String key) {
            for (ServiceStatus s : values()) {
                if (s.name().equals(key)) {
                    return s;
                }
            }
            return UNKNOWN;
        }
    }
}
