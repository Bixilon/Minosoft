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
package de.bixilon.minosoft.data.locale.minecraft

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent

open class MinecraftLanguage : Translator {
    val language: String
    private val data = HashMap<String, String>()

    constructor(language: String, json: JsonObject) {
        this.language = language
        for ((key, value) in json.entrySet()) {
            data[key] = value.asString
        }
    }

    constructor(language: String, data: String) {
        this.language = language
        for (line in data.lines().toTypedArray()) {
            if (line.isBlank()) {
                continue
            }
            val splitLine = line.split("=", limit = 2).toTypedArray()
            this.data[splitLine[0]] = splitLine[1]
        }
    }

    fun canTranslate(key: String?): Boolean {
        return data.containsKey(key)
    }

    override fun translate(key: String?, parent: TextComponent?, vararg data: Any?): ChatComponent {
        val placeholder = this.data[key] ?: return ChatComponent.valueOf(null, parent, key.toString() + "->" + data.toString())

        val ret = BaseComponent()

        val arguments: MutableList<Any?> = mutableListOf()
        var splitPlaceholder: List<String> = listOf()
        FORMATTER_ORDER_REGEX.find(placeholder)?.let {
            if (it.groupValues.isEmpty()) {
                // this is not the correct formatter
                return@let
            }
            splitPlaceholder = placeholder.split(FORMATTER_ORDER_REGEX)
            for ((index, part) in it.groupValues.withIndex()) {
                if (index % 2 == 0) {
                    continue
                }
                val dataIndex = part.toInt() - 1
                if (dataIndex < 0 || dataIndex > data.size) {
                    arguments += null
                    continue
                }
                arguments += data[dataIndex]
            }
        }


        placeholder.split(FORMATTER_SPLIT_REGEX).let {
            if (splitPlaceholder.isEmpty()) {
                splitPlaceholder = it
            }
            arguments.addAll(data.toList())
        }

        for ((index, part) in splitPlaceholder.withIndex()) {
            ret.parts.add(ChatComponent.valueOf(this, parent, part))
            if (index < data.size) {
                ret.parts.add(ChatComponent.valueOf(this, parent, arguments[index]))
            }
        }

        return ret
    }

    override fun toString(): String {
        return language
    }

    companion object {
        private val FORMATTER_ORDER_REGEX = "%(\\w+)\\\$[sd]".toRegex() // %1$s fell from a high place
        private val FORMATTER_SPLIT_REGEX = "%[ds]".toRegex() // %s fell from a high place
    }
}
