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

import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.text.events.click.ClickEvent
import de.bixilon.minosoft.data.text.events.hover.HoverEvent
import de.bixilon.minosoft.data.text.formatting.*
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.util.Duration


open class TextComponent(
    message: Any? = "",
    override var color: RGBColor? = null,
    @Deprecated("bitfield")
    override val formatting: MutableSet<ChatFormattingCode> = mutableSetOf(),
    var clickEvent: ClickEvent? = null,
    var hoverEvent: HoverEvent? = null,
) : ChatComponent, TextStyle {
    override var message: String = message.toString().replace(ProtocolDefinition.TEXT_COMPONENT_FORMATTING_PREFIX, '&')

    override fun obfuscate(): TextComponent {
        formatting.add(PreChatFormattingCodes.OBFUSCATED); return this
    }

    override fun bold(): TextComponent {
        formatting.add(PreChatFormattingCodes.BOLD); return this
    }

    override fun strikethrough(): TextComponent {
        formatting.add(PreChatFormattingCodes.STRIKETHROUGH); return this
    }

    override fun underline(): TextComponent {
        formatting.add(PreChatFormattingCodes.UNDERLINED); return this
    }

    override fun italic(): TextComponent {
        formatting.add(PreChatFormattingCodes.ITALIC); return this
    }

    fun clickEvent(clickEvent: ClickEvent?): TextComponent {
        this.clickEvent = clickEvent
        return this
    }

    fun hoverEvent(hoverEvent: HoverEvent?): TextComponent {
        this.hoverEvent = hoverEvent
        return this
    }

    fun color(color: RGBColor): TextComponent {
        this.color = color
        return this
    }

    override fun toString(): String {
        return legacyText
    }

    override fun setFallbackColor(color: RGBColor): TextComponent {
        if (this.color == null) {
            this.color = color
        }
        return this
    }

    override val ansiColoredMessage: String
        get() {
            val stringBuilder = StringBuilder()
            this.color?.let {
                stringBuilder.append(it.ansi)
            }

            for (formattingCode in this.formatting) {
                stringBuilder.append(formattingCode.ansi)
            }
            stringBuilder.append(this.message)
            stringBuilder.append(PostChatFormattingCodes.RESET)
            return stringBuilder.toString()
        }

    override val legacyText: String
        get() {
            val stringBuilder = StringBuilder()
            color?.let {
                val colorChar = ChatCode.FORMATTING_CODES_ID.indexOf(it)
                if (colorChar != -1) {
                    stringBuilder.append(ProtocolDefinition.TEXT_COMPONENT_FORMATTING_PREFIX).append(Integer.toHexString(colorChar))
                }
            }
            for (formattingCode in this.formatting) {
                stringBuilder.append(ProtocolDefinition.TEXT_COMPONENT_FORMATTING_PREFIX)
                stringBuilder.append(formattingCode.char)
            }
            stringBuilder.append(this.message)
            stringBuilder.append(ProtocolDefinition.TEXT_COMPONENT_FORMATTING_PREFIX).append(PostChatFormattingCodes.RESET.char) // ToDo: This should not always be appended
            return stringBuilder.toString()
        }

    override fun getJavaFXText(nodes: ObservableList<Node>): ObservableList<Node> {
        val text = Text(this.message)
        this.color?.let {
            if (ErosProfileManager.selected.text.colored) {
                text.fill = Color.rgb(it.red, it.green, it.blue)
            }
        } ?: let {
            text.styleClass += "text-default-color"
        }
        for (chatFormattingCode in formatting) {
            when (chatFormattingCode) {
                PreChatFormattingCodes.OBFUSCATED -> {
                    // ToDo: This is just slow
                    val obfuscatedTimeline = if (ErosProfileManager.selected.text.obfuscated) {
                        Timeline(
                            KeyFrame(Duration.millis(50.0), {
                                val chars = text.text.toCharArray()
                                for (i in chars.indices) {
                                    chars[i] = ProtocolDefinition.OBFUSCATED_CHARS.random()
                                }
                                text.text = String(chars)
                            }),
                        )
                    } else {
                        Timeline(
                            KeyFrame(Duration.millis(500.0), {
                                text.isVisible = false
                            }),
                            KeyFrame(Duration.millis(1000.0), {
                                text.isVisible = true
                            }),
                        )
                    }

                    obfuscatedTimeline.cycleCount = Animation.INDEFINITE
                    obfuscatedTimeline.play()
                    text.styleClass.add("obfuscated")
                }

                PreChatFormattingCodes.BOLD -> {
                    text.style += "-fx-font-weight: bold;"
                }

                PreChatFormattingCodes.STRIKETHROUGH -> {
                    text.style += "-fx-strikethrough: true;"
                }

                PreChatFormattingCodes.UNDERLINED -> {
                    text.style += "-fx-underline: true;"
                }

                PreChatFormattingCodes.ITALIC -> {
                    text.style += "-fx-font-weight: italic;"
                }
            }
        }
        nodes.add(text)

        clickEvent?.applyJavaFX(text)
        hoverEvent?.applyJavaFX(text)
        return nodes
    }

    override fun getJson(): Any {
        if (message.isEmpty()) {
            return emptyMap<String, Any>()
        }
        val json: MutableJsonObject = mutableMapOf(
            "text" to message
        )

        if (PreChatFormattingCodes.OBFUSCATED in formatting) json["obfuscated"] = true
        if (PreChatFormattingCodes.BOLD in formatting) json["bold"] = true
        if (PreChatFormattingCodes.STRIKETHROUGH in formatting) json["strikethrough"] = true
        if (PreChatFormattingCodes.UNDERLINED in formatting) json["underlined"] = true
        if (PreChatFormattingCodes.ITALIC in formatting) json["italic"] = true


        color?.let { json["color"] = ChatColors.NAME_MAP.getKey(it) ?: it.toString() }

        // TODO: hover, click event

        return json
    }

    fun copy(message: Any? = this.message, color: RGBColor? = this.color, formatting: MutableSet<ChatFormattingCode> = this.formatting, clickEvent: ClickEvent? = this.clickEvent, hoverEvent: HoverEvent? = this.hoverEvent): TextComponent {
        return TextComponent(
            message = message,
            color = color,
            formatting = formatting,
            clickEvent = clickEvent,
            hoverEvent = hoverEvent,
        )
    }

    override val length: Int
        get() = message.length

    override fun getTextAt(pointer: Int): TextComponent {
        if (pointer < 0 || pointer > message.length) {
            throw IllegalArgumentException("Pointer out of bounds: $pointer")
        }
        return this
    }

    override fun cut(length: Int) {
        if (length <= 0) {
            throw IllegalArgumentException("Can not cut <= 0: $length")
        }
        if (length >= message.length) {
            throw IllegalArgumentException("Can not cut beyond length: $length >= ${message.length}")
        }

        message = message.substring(0, length)
    }

    override fun hashCode(): Int {
        return message.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is TextComponent) {
            return false
        }
        return message == other.message && color == other.color && formatting == other.formatting && clickEvent == other.clickEvent && hoverEvent == other.hoverEvent
    }

    override fun trim(): ChatComponent? {
        if (message.isEmpty()) return null
        return this
    }
}
