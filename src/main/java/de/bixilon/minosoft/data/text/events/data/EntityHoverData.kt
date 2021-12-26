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

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.util.KUtil.asUUID
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound
import java.util.*

class EntityHoverData(
    val uuid: UUID,
    val resourceLocation: ResourceLocation?,
    val name: ChatComponent,
) {

    companion object {
        fun deserialize(data: Any): EntityHoverData {
            var json: Map<String, Any> = if (data is String) {
                Jackson.MAPPER.readValue(data, Jackson.JSON_MAP_TYPE)
            } else {
                data
            }.asCompound()
            json["text"]?.let {
                // 1.14.3.... lol
                json = Jackson.MAPPER.readValue(it.unsafeCast<String>(), Jackson.JSON_MAP_TYPE)
            }
            var type: ResourceLocation? = null
            json["type"]?.nullCast<String>()?.let {
                type = it.toResourceLocation()
            }

            return EntityHoverData(json["id"].unsafeCast<String>().asUUID(), type, ChatComponent.of(json["name"]))
        }
    }
}
