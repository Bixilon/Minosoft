/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.text.events.hover

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.protocol.buffers.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.buffers.OutByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.json.Jackson
import java.util.*

data class EntityHoverEvent(
    val uuid: UUID,
    val type: ResourceLocation?,
    val name: ChatComponent,
) : HoverEvent {

    companion object : HoverEventFactory<EntityHoverEvent> {
        override val name: String = "show_entity"

        override fun build(json: JsonObject, restricted: Boolean): EntityHoverEvent {
            val data = json.data
            var json: JsonObject = if (data is String) {
                Jackson.MAPPER.readValue(data, Jackson.JSON_MAP_TYPE)
            } else {
                data.unsafeCast()
            }
            json["text"]?.let {// minecraft (e.g. 1.14) is boxing the actual data in an nbt string
                when (it) {
                    is String -> json = Jackson.MAPPER_LENIENT.readValue(it, Jackson.JSON_MAP_TYPE)
                    is Map<*, *> -> json = it.unsafeCast()
                }
            }
            var type: ResourceLocation? = null
            json["type"]?.nullCast<String>()?.let {
                type = it.toResourceLocation()
            }

            val rawUUID = json["id"]
            val uuid = if (rawUUID is IntArray) InByteBuffer(OutByteBuffer().apply { writeBareIntArray(rawUUID) }.toArray()).readUUID() else rawUUID.toString().toUUID()

            return EntityHoverEvent(uuid, type, ChatComponent.of(json["name"]))
        }
    }
}
