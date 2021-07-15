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

package de.bixilon.minosoft.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public final class HTTP {

    public static HttpResponse<String> postJson(String url, String json, HashMap<String, String> headers) throws IOException, InterruptedException {
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .headers(Util.headersMapToArray(headers))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> postJson(String url, JsonObject json) throws IOException, InterruptedException {
        return postJson(url, Util.GSON.toJson(json), new HashMap<>());
    }

    public static HttpResponse<String> postJson(String url, String json) throws IOException, InterruptedException {
        return postJson(url, json, new HashMap<>());
    }

    public static HttpResponse<String> get(String url, HashMap<String, String> headers) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers(Util.headersMapToArray(headers))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> get(String url) throws IOException, InterruptedException {
        return get(url, new HashMap<>());
    }


    public static JsonElement getJson(String url, HashMap<String, String> headers) throws IOException, InterruptedException {
        HttpResponse<String> response = get(url, headers);
        if (response.statusCode() != 200) {
            throw new IOException();
        }
        return JsonParser.parseString(response.body());
    }

    public static JsonElement getJson(String url) throws IOException, InterruptedException {
        return getJson(url, new HashMap<>());
    }

    public static HttpResponse<String> postData(String url, HashMap<String, String> data) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(Util.mapToUrlQuery(data)))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
