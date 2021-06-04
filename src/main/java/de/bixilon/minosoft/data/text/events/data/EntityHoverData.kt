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
package de.bixilon.minosoft.data.text.events.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.asUUID
import java.util.*

class EntityHoverData(
    val uuid: UUID,
    val resourceLocation: ResourceLocation?,
    val name: ChatComponent,
) {

    companion object {
        fun deserialize(data: JsonElement): EntityHoverData {
            var json = if (data is JsonPrimitive) {
                JsonParser.parseString(data.getAsString()).asJsonObject
            } else {
                data as JsonObject
            }
            json["text"]?.let {
                // 1.14.3.... lol
                json = JsonParser.parseString(json["text"].asString).asJsonObject
            }
            var type: ResourceLocation? = null
            json["type"]?.asString?.let {
                type = it.asResourceLocation()
            }

            return EntityHoverData(json["id"].asString.asUUID(), type, ChatComponent.of(json["name"]))
        }
    }
}
