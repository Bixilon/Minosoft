/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.http

import de.bixilon.kutil.collections.CollectionUtil.extend
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.json.Jackson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object HTTP2 {

    fun Map<String, Any>.headers(): Array<String> {
        val headers: MutableList<String> = mutableListOf()

        for ((key, value) in this) {
            headers += key
            headers += value.toString()
        }
        return headers.toTypedArray()
    }

    fun <Payload, Response> post(url: String, data: Payload, bodyPublisher: (Payload) -> String, bodyBuilder: (String) -> Response, headers: Map<String, Any> = mapOf()): HTTPResponse<Response> {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(bodyPublisher(data)))
            .headers(*headers.headers())
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return HTTPResponse(response.statusCode(), bodyBuilder(response.body()))
    }


    fun Map<String, Any>.postJson(url: String, headers: Map<String, Any> = mapOf()): HTTPResponse<Map<String, Any>?> {
        return post(
            url = url,
            data = this,
            bodyPublisher = { Jackson.MAPPER.writeValueAsString(it) },
            bodyBuilder = { it.isBlank().decide(null) { Jackson.MAPPER.readValue(it, Jackson.JSON_MAP_TYPE) as Map<String, Any> } },
            headers = headers.extend(
                "Content-Type" to "application/json",
                "Accept" to "application/json",
            )
        )
    }

    fun Map<String, Any>.postData(url: String, headers: Map<String, Any> = mapOf()): HTTPResponse<Map<String, Any>?> {
        return post(
            url = url,
            data = this,
            bodyPublisher = { Util.mapToUrlQuery(this) },
            bodyBuilder = { it.isBlank().decide(null) { Jackson.MAPPER.readValue(it, Jackson.JSON_MAP_TYPE) as Map<String, Any> } },
            headers = headers.extend(
                "Content-Type" to "application/x-www-form-urlencoded",
            )
        )
    }

    fun <Response> String.get(bodyBuilder: (String) -> Response, headers: Map<String, Any> = mapOf()): HTTPResponse<Response> {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(this))
            .GET()
            .headers(*headers.headers())
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return HTTPResponse(response.statusCode(), bodyBuilder(response.body()))
    }

    fun String.getJson(headers: Map<String, Any> = mapOf()): HTTPResponse<Map<String, Any>?> {
        return this.get(
            bodyBuilder = { it.isBlank().decide(null) { Jackson.MAPPER.readValue(it, Jackson.JSON_MAP_TYPE) as Map<String, Any> } },
            headers = headers.extend(
                "Content-Type" to "application/json",
                "Accept" to "application/json",
            )
        )
    }
}
