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

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.locale.minecraft.Translator
import de.bixilon.minosoft.data.text.RGBColor.Companion.asColor
import de.bixilon.minosoft.data.text.events.ClickEvent
import de.bixilon.minosoft.data.text.events.HoverEvent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.text.TextGetProperties
import de.bixilon.minosoft.gui.rendering.font.text.TextSetProperties
import de.bixilon.minosoft.gui.rendering.hud.nodes.primitive.LabelNode
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.KUtil.nullCast
import glm_.vec2.Vec2i
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

    constructor(parent: TextComponent? = null, legacy: String = "") {
        val currentText = StringBuilder()
        var currentColor = parent?.color
        val currentFormatting: MutableSet<ChatFormattingCode> = parent?.formatting?.toMutableSet() ?: mutableSetOf()

        val iterator = StringCharacterIterator(legacy)

        var char = iterator.first()


        fun push() {
            if (currentText.isNotEmpty()) {
                parts.add(TextComponent(message = currentText.toString(), color = currentColor, formatting = currentFormatting.toMutableSet()))
                currentColor = null
                currentText.clear()
            }
            currentFormatting.clear()
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
                // just append it as special char
                currentText.append(char)
                currentText.append(formattingChar)
            }

            char = iterator.next()
        }

        push()
    }

    constructor(translator: Translator? = null, parent: TextComponent? = null, json: JsonObject) {
        val currentParent: TextComponent?
        var currentText = ""
        json["text"]?.asString?.let {
            if (it.indexOf(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR) != -1) {
                parts.add(ChatComponent.of(it, translator, parent))
                return
            }
            currentText = it
        }

        val color = json["color"]?.asString?.let { colorName ->
            if (colorName.startsWith("#")) {
                colorName.asColor()
            } else {
                ChatCode.FORMATTING_CODES[colorName].nullCast<RGBColor>()
            }
        } ?: parent?.color

        val formatting = parent?.formatting?.toMutableSet() ?: mutableSetOf()

        formatting.addOrRemove(PreChatFormattingCodes.BOLD, json["bold"]?.asBoolean)
        formatting.addOrRemove(PreChatFormattingCodes.ITALIC, json["italic"]?.asBoolean)
        formatting.addOrRemove(PreChatFormattingCodes.UNDERLINED, json["underlined"]?.asBoolean)
        formatting.addOrRemove(PreChatFormattingCodes.STRIKETHROUGH, json["strikethrough"]?.asBoolean)
        formatting.addOrRemove(PreChatFormattingCodes.OBFUSCATED, json["obfuscated"]?.asBoolean)

        val clickEvent = json["clickEvent"]?.asJsonObject?.let { click -> ClickEvent(click) }
        val hoverEvent = json["hoverEvent"]?.asJsonObject?.let { hover -> HoverEvent(hover) }

        val textComponent = MultiChatComponent(
            message = currentText,
            color = color,
            formatting = formatting,
            clickEvent = clickEvent,
            hoverEvent = hoverEvent,
        )
        if (currentText.isNotEmpty()) {
            parts.add(textComponent)
        }
        currentParent = textComponent


        json["extra"]?.asJsonArray?.let {
            for (data in it) {
                parts.add(ChatComponent.of(data, translator, currentParent))
            }
        }


        json["translate"]?.asString?.let {
            val with: MutableList<JsonElement> = mutableListOf()
            json["with"]?.asJsonArray?.let { withArray ->
                for (part in withArray) {
                    with.add(part)
                }
            }
            parts.add(translator?.translate(it, currentParent, *with.toTypedArray()) ?: ChatComponent.of(json["with"], translator, currentParent))
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

    override fun prepareRender(startPosition: Vec2i, offset: Vec2i, renderWindow: RenderWindow, textElement: LabelNode, z: Int, setProperties: TextSetProperties, getProperties: TextGetProperties) {
        for (part in parts) {
            part.prepareRender(startPosition, offset, renderWindow, textElement, z, setProperties, getProperties)
        }
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
}
