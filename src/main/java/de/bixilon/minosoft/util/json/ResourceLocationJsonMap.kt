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

package de.bixilon.minosoft.util.json

import com.google.gson.JsonObject
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.registries.ResourceLocation

object ResourceLocationJsonMap {

    fun JsonObject.toResourceLocationMap(): Map<ResourceLocation, JsonObject> {
        val ret: MutableMap<ResourceLocation, JsonObject> = mutableMapOf()

        for ((key, value) in entrySet()) {
            check(value is JsonObject)

            ret[ResourceLocation(key)] = value
        }

        return ret
    }

    fun Map<*, *>.toResourceLocationMap(): Map<ResourceLocation, Any> {
        val ret: MutableMap<ResourceLocation, Any> = mutableMapOf()

        for ((key, value) in this) {
            ret[ResourceLocation(key.unsafeCast<String>())] = value.unsafeCast<Any>()
        }

        return ret
    }
}
