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

package de.bixilon.minosoft.data.registries.chat

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.asJsonList
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.minosoft.data.text.ChatComponent

data class TypeProperties(
    val translationKey: String,
    val parameters: List<ChatParameter>,
    val style: Map<String, Any>,
) {

    fun formatParameters(parameters: Map<ChatParameter, ChatComponent>): Array<ChatComponent> {
        val output: MutableList<ChatComponent> = mutableListOf()

        for (parameter in this.parameters) {
            output += parameters[parameter] ?: continue
        }

        return output.toTypedArray()
    }

    companion object {

        fun deserialize(data: JsonObject): TypeProperties {
            val decoration = data["decoration"]?.toJsonObject() ?: data

            val key = decoration["translation_key"]!!.toString()
            var parameters: List<ChatParameter> = emptyList()

            decoration["parameters"]?.asJsonList()?.let {
                val list: MutableList<ChatParameter> = mutableListOf()
                for (entry in it) {
                    list += ChatParameter[entry.toString()]
                }
                parameters = list
            }
            val style = decoration["style"]?.asJsonObject() ?: emptyMap()

            return TypeProperties(key, parameters, style)
        }
    }
}
