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

package de.bixilon.minosoft.data.text

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.json.JsonUtil.toJsonList
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.minosoft.data.language.Translator
import de.bixilon.minosoft.data.text.ChatCode.Companion.toColor
import de.bixilon.minosoft.data.text.events.click.ClickEvent
import de.bixilon.minosoft.data.text.events.click.ClickEvents
import de.bixilon.minosoft.data.text.events.click.OpenURLClickEvent
import de.bixilon.minosoft.data.text.events.hover.HoverEvents
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.get
import javafx.collections.ObservableList
import javafx.scene.Node
import java.text.CharacterIterator
import java.text.StringCharacterIterator

class BaseComponent : ChatComponent {
    val parts: MutableList<ChatComponent> = mutableListOf()

    constructor(vararg parts: Any?) {
        for (part in parts) {
            this.parts += part.format()
        }
    }

    constructor(parent: TextComponent? = null, legacy: String = "", restrictedMode: Boolean = false) {
        val currentText = StringBuilder()
        var currentColor = parent?.color
        var currentFormatting: MutableSet<ChatFormattingCode> = parent?.formatting?.toMutableSet() ?: TextComponent.DEFAULT_FORMATTING.toMutableSet()

        val iterator = StringCharacterIterator(legacy)

        var char = iterator.first()


        fun push() {
            if (currentText.isEmpty()) {
                return
            }
            val spaceSplit = currentText.split(' ')
            var currentMessage = ""

            fun push(clickEvent: ClickEvent?) {
                if (currentMessage.isEmpty()) {
                    return
                }
                parts += TextComponent(message = currentMessage, color = currentColor, formatting = currentFormatting.toMutableSet(), clickEvent = clickEvent)
                currentMessage = ""
            }

            for ((index, split) in spaceSplit.withIndex()) {
                var clickEvent: ClickEvent? = null
                if (split.isNotBlank()) {
                    for (protocol in URLProtocols.VALUES) {
                        if (!split.startsWith(protocol.prefix)) {
                            continue
                        }
                        if (protocol.restricted && restrictedMode) {
                            break
                        }
                        clickEvent = OpenURLClickEvent(split.toURL())
                        break
                    }
                }
                if (split.isNotEmpty()) {
                    if (clickEvent != null) {
                        // push previous
                        push(null)

                        currentMessage = split
                        push(clickEvent)
                    } else {
                        currentMessage += split
                    }
                }

                if (index != spaceSplit.size - 1) {
                    currentMessage += " "
                }
            }
            push(null)
            currentFormatting = TextComponent.DEFAULT_FORMATTING.toMutableSet()
            currentColor = null
            currentText.clear()
        }

        while (char != CharacterIterator.DONE) {
            if (char != ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR) {
                currentText.append(char)
                char = iterator.next()
                continue
            }

            val formattingChar = iterator.next()

            ChatColors.VALUES.getOrNull(Character.digit(formattingChar, 16))?.let {
                push()
                currentColor = it.nullCast<RGBColor>()
            } ?: ChatFormattingCodes.getChatFormattingCodeByChar(formattingChar)?.let {
                push()

                if (it == PostChatFormattingCodes.RESET) {
                    push()
                } else {
                    currentFormatting.add(it)
                }
            } ?: let {
                // ignore and ignore next char
                char = iterator.next()
            }
            // check because of above
            if (char == CharacterIterator.DONE) {
                break
            }
            char = iterator.next()
        }

        push()
    }

    constructor(translator: Translator? = null, parent: TextComponent? = null, json: Map<String, Any>, restrictedMode: Boolean = false) {
        var currentParent: TextComponent? = null
        var currentText = ""

        fun parseExtra() {
            json["extra"].toJsonList()?.let {
                for (data in it) {
                    parts += ChatComponent.of(data, translator, currentParent)
                }
            }
        }

        json["text"]?.nullCast<String>()?.let {
            if (it.indexOf(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR) != -1) {
                this += ChatComponent.of(it, translator, parent)
                parseExtra()
                return
            }
            currentText = it
        }


        val color = json["color"]?.nullCast<String>()?.toColor() ?: parent?.color

        val formatting = parent?.formatting?.toMutableSet() ?: TextComponent.DEFAULT_FORMATTING.toMutableSet()

        formatting.addOrRemove(PreChatFormattingCodes.BOLD, json["bold"]?.toBoolean())
        formatting.addOrRemove(PreChatFormattingCodes.ITALIC, json["italic"]?.toBoolean())
        formatting.addOrRemove(PreChatFormattingCodes.UNDERLINED, json["underlined"]?.toBoolean())
        formatting.addOrRemove(PreChatFormattingCodes.STRIKETHROUGH, json["strikethrough"]?.toBoolean())
        formatting.addOrRemove(PreChatFormattingCodes.OBFUSCATED, json["obfuscated"]?.toBoolean())
        formatting.addOrRemove(PreChatFormattingCodes.SHADOWED, json["shadowed"]?.toBoolean())

        val clickEvent = json["clickEvent", "click_event"]?.toJsonObject()?.let { click -> ClickEvents.build(click, restrictedMode) }
        val hoverEvent = json["hoverEvent", "hover_event"]?.toJsonObject()?.let { hover -> HoverEvents.build(hover, restrictedMode) }

        val textComponent = TextComponent(
            message = currentText,
            color = color,
            formatting = formatting,
            clickEvent = clickEvent,
            hoverEvent = hoverEvent,
        )
        if (currentText.isNotEmpty()) {
            parts += textComponent
        }
        currentParent = textComponent

        parseExtra()

        json["translate"]?.toString()?.let {
            val with: MutableList<Any> = mutableListOf()
            json["with"].toJsonList()?.let { withArray ->
                for (part in withArray) {
                    with.add(part ?: continue)
                }
            }
            parts += translator?.translate(it.toResourceLocation(), currentParent, *with.toTypedArray()) ?: ChatComponent.of(json["with"], translator, currentParent)
        }
    }

    override val ansiColoredMessage: String
        get() {
            val stringBuilder = StringBuilder()
            for (part in parts) {
                stringBuilder.append(part.ansiColoredMessage)
            }
            return stringBuilder.toString()
        }

    override val legacyText: String
        get() {
            val stringBuilder = StringBuilder()
            for (part in parts) {
                stringBuilder.append(part.legacyText)
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

    override fun applyDefaultColor(color: RGBColor) {
        for (part in parts) {
            part.applyDefaultColor(color)
        }
    }

    override fun toString(): String {
        return legacyText
    }

    operator fun plusAssign(text: Any?) {
        parts += text.format()
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
        throw IllegalArgumentException("Pointer ot of bounds!")
    }
}
