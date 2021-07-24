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
package de.bixilon.minosoft.data.language

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent

class Language(
    val name: String,
    private val data: Map<ResourceLocation, String>,
) : Translator {

    override fun canTranslate(key: ResourceLocation?): Boolean {
        return data.containsKey(key)
    }

    override fun translate(key: ResourceLocation?, parent: TextComponent?, vararg data: Any?): ChatComponent {
        val placeholder = this.data[key] ?: return ChatComponent.of(key.toString() + "->" + data.toString(), null, parent)

        val ret = BaseComponent()

        val arguments: MutableList<Any?> = mutableListOf()
        var splitPlaceholder: List<String> = listOf()

        // Bring arguments in correct oder
        FORMATTER_ORDER_REGEX.findAll(placeholder).toList().let {
            if (it.isEmpty()) {
                // this is not the correct formatter
                return@let
            }
            splitPlaceholder = placeholder.split(FORMATTER_ORDER_REGEX)
            for (matchResult in it) {
                // 2 groups: Full, index. We don't care about the full value, just skip it
                val dataIndex = matchResult.groupValues[1].toInt() - 1
                if (dataIndex < 0 || dataIndex > data.size) {
                    arguments += null
                    continue
                }
                arguments += data[dataIndex]
            }
        }

        // check if other splitter already did the job for us
        if (splitPlaceholder.isEmpty()) {
            placeholder.split(FORMATTER_SPLIT_REGEX).let {
                splitPlaceholder = it
                arguments.addAll(data.toList())
            }
        }

        // create base component
        for ((index, part) in splitPlaceholder.withIndex()) {
            ret += ChatComponent.of(part, this, parent)
            if (index < data.size) {
                ret += ChatComponent.of(arguments[index], this, parent)
            }
        }

        return ret
    }

    override fun toString(): String {
        return name
    }

    companion object {
        private val FORMATTER_ORDER_REGEX = "%(\\w+)\\\$[sd]".toRegex() // %1$s fell from a high place
        private val FORMATTER_SPLIT_REGEX = "%[ds]".toRegex() // %s fell from a high place
    }
}
