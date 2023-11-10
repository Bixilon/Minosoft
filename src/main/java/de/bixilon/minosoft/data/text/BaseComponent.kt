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

package de.bixilon.minosoft.data.text

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonList
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.data.language.translate.Translator
import de.bixilon.minosoft.data.text.events.click.ClickEvents
import de.bixilon.minosoft.data.text.events.hover.HoverEvents
import de.bixilon.minosoft.data.text.formatting.FormattingCodes
import de.bixilon.minosoft.data.text.formatting.TextFormatting
import de.bixilon.minosoft.data.text.formatting.color.ChatColors.toColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.get
import javafx.collections.ObservableList
import javafx.scene.Node

class BaseComponent : ChatComponent {
    val parts: MutableList<ChatComponent> = mutableListOf()

    constructor(parts: MutableList<ChatComponent>) {
        this.parts += parts
    }

    constructor(vararg parts: Any?) {
        for (part in parts) {
            this.parts += part.format()
        }
    }

    constructor(translator: Translator? = null, parent: TextComponent? = null, json: Map<String, Any>, restricted: Boolean = false) {
        val color = json["color"]?.nullCast<String>()?.toColor() ?: parent?.color

        val formatting = parent?.formatting?.copy() ?: TextFormatting()

        json["bold"]?.toBoolean()?.let { formatting[FormattingCodes.BOLD] = it }
        json["italic"]?.toBoolean()?.let { formatting[FormattingCodes.ITALIC] = it }
        json["underlined"]?.toBoolean()?.let { formatting[FormattingCodes.UNDERLINED] = it }
        json["strikethrough"]?.toBoolean()?.let { formatting[FormattingCodes.STRIKETHROUGH] = it }
        json["obfuscated"]?.toBoolean()?.let { formatting[FormattingCodes.OBFUSCATED] = it }

        val font = json["font"]?.toResourceLocation() ?: parent?.font
        val clickEvent = json["clickEvent", "click_event"]?.toJsonObject()?.let { click -> ClickEvents.build(click, restricted) } ?: parent?.clickEvent
        val hoverEvent = json["hoverEvent", "hover_event"]?.toJsonObject()?.let { hover -> HoverEvents.build(hover, restricted) } ?: parent?.hoverEvent

        val text = json["text"]?.nullCast<String>() ?: ""

        val component = TextComponent(
            message = text,
            color = color,
            formatting = formatting,
            font = font,
            clickEvent = clickEvent,
            hoverEvent = hoverEvent,
        )


        fun parseExtra() {
            json["extra"].toJsonList()?.let {
                for (data in it) {
                    this += ChatComponent.of(data, translator, component, restricted)
                }
            }
        }

        if (text.indexOf(ProtocolDefinition.TEXT_COMPONENT_FORMATTING_PREFIX) != -1) {
            this += ChatComponent.of(text, translator, component, restricted)
            parseExtra()
            return
        }

        if (text.isNotEmpty()) {
            this += component
        }

        parseExtra()

        json["translate"]?.toString()?.let {
            val with: Array<ChatComponent> = json.with(translator, component, restricted) ?: emptyArray()
            val fallback = json["fallback"]?.toString()
            this += translator?.forceTranslate(it.toResourceLocation(), component, restricted, fallback, data = with.unsafeCast()) ?: ChatComponent.of(json["with"], translator, component, restricted)
        }
    }

    override fun getJson(): Any {
        if (parts.isEmpty()) {
            return emptyList<Any>()
        }
        if (parts.size == 1) {
            return parts.first().getJson()
        }
        val list = mutableListOf<Any>()
        for (part in parts) {
            list += part.getJson()
        }
        return list
    }

    override fun cut(length: Int) {
        if (length <= 0) {
            throw IllegalArgumentException("Can not cut <= 0: $length")
        }
        var remaining = length
        for (part in parts) {
            val partLength = part.length
            if (remaining - partLength < 0) {
                part.cut(remaining)
                break
            }
            remaining -= partLength
        }
    }

    override fun copy(): ChatComponent {
        val parts: MutableList<ChatComponent> = mutableListOf()
        for (part in this.parts) {
            parts += part.copy()
        }

        return BaseComponent(parts)
    }

    override val ansi: String
        get() {
            val builder = StringBuilder()
            for (part in parts) {
                builder.append(part.ansi)
            }
            return builder.toString()
        }

    override val legacy: String
        get() {
            val stringBuilder = StringBuilder()
            for (part in parts) {
                stringBuilder.append(part.legacy)
            }
            // ToDo: Remove §r suffix
            return stringBuilder.toString()
        }

    override val message: String
        get() {
            val stringBuilder = StringBuilder()
            for (part in parts) {
                stringBuilder.append(part.message)
            }
            return stringBuilder.toString()
        }

    override fun getJavaFXText(nodes: ObservableList<Node>): ObservableList<Node> {
        for (part in parts) {
            part.getJavaFXText(nodes)
        }
        return nodes
    }

    override fun obfuscate(): BaseComponent {
        for (part in parts) part.obfuscate(); return this
    }

    override fun bold(): BaseComponent {
        for (part in parts) part.bold(); return this
    }

    override fun strikethrough(): BaseComponent {
        for (part in parts) part.strikethrough(); return this
    }

    override fun underline(): BaseComponent {
        for (part in parts) part.underline(); return this
    }

    override fun italic(): BaseComponent {
        for (part in parts) part.italic(); return this
    }

    override fun setFallbackColor(color: RGBColor): BaseComponent {
        for (part in parts) part.setFallbackColor(color); return this
    }

    override fun toString(): String {
        return legacy
    }

    operator fun plusAssign(component: ChatComponent) {
        if (component.length == 0) {
            return
        }
        parts += component
    }

    operator fun plusAssign(text: Any?) {
        this += text.format()
    }

    private fun <T> MutableSet<T>.addOrRemove(value: T, addOrRemove: Boolean?) {
        if (addOrRemove == null) {
            return
        }
        if (addOrRemove) {
            this.add(value)
        } else {
            this.remove(value)
        }
    }

    override fun hashCode(): Int {
        return parts.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is BaseComponent) {
            return false
        }
        return parts == other.parts
    }

    override val length: Int
        get() {
            var length = 0
            for (part in parts) {
                length += part.length
            }
            return length
        }

    override fun getTextAt(pointer: Int): TextComponent {
        var pointer = pointer
        for (part in parts) {
            val length = part.length
            if (pointer <= length) {
                return part.getTextAt(pointer)
            }
            pointer -= length
        }
        throw IllegalArgumentException("Pointer out of bounds!")
    }

    override fun trim(): ChatComponent? {
        return when {
            parts.isEmpty() -> null
            parts.size == 1 -> parts.first().trim()
            else -> {
                val parts: MutableList<ChatComponent> = mutableListOf()
                for (part in this.parts) {
                    parts += part.trim() ?: continue
                }
                if (parts.isEmpty()) return null
                if (parts.size == 1) parts.first()
                if (this.parts.size == parts.size) return this
                return BaseComponent(parts)
            }
        }
    }

    private fun JsonObject.with(translator: Translator?, parent: TextComponent, restricted: Boolean): Array<ChatComponent>? {
        val with = this["with"]?.toJsonList() ?: return null
        val parts: MutableList<ChatComponent> = mutableListOf()
        for (part in with) {
            if (part == null) continue
            parts += ChatComponent.of(raw = part, translator, parent, restricted = restricted)
        }

        return parts.toTypedArray()
    }
}
