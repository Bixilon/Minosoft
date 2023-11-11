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

import de.bixilon.kutil.enums.BitEnumSet
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.events.click.ClickEvent
import de.bixilon.minosoft.data.text.events.hover.HoverEvent
import de.bixilon.minosoft.data.text.formatting.FormattingCodes
import de.bixilon.minosoft.data.text.formatting.TextStyle
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
import java.util.concurrent.atomic.AtomicInteger


open class TextComponent(
    message: Any? = "",
    override var color: RGBColor? = null,
    override val formatting: BitEnumSet<FormattingCodes> = FormattingCodes.set(),
    var font: ResourceLocation? = null,
    var clickEvent: ClickEvent? = null,
    var hoverEvent: HoverEvent? = null,
) : ChatComponent, TextStyle {
    override var message: String = message.toString().replace(ProtocolDefinition.TEXT_COMPONENT_FORMATTING_PREFIX, '&')

    override fun obfuscate(): TextComponent {
        formatting += FormattingCodes.OBFUSCATED; return this
    }

    override fun bold(): TextComponent {
        formatting += FormattingCodes.BOLD; return this
    }

    override fun strikethrough(): TextComponent {
        formatting += FormattingCodes.STRIKETHROUGH; return this
    }

    override fun underline(): TextComponent {
        formatting += FormattingCodes.UNDERLINED; return this
    }

    override fun italic(): TextComponent {
        formatting += FormattingCodes.ITALIC; return this
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
        return legacy
    }

    override fun setFallbackColor(color: RGBColor): TextComponent {
        if (this.color == null) {
            this.color = color
        }
        return this
    }

    override val ansi: String
        get() {
            val builder = StringBuilder()
            this.color?.let {
                builder.append(it.ansi)
            }

            for (formattingCode in this.formatting) {
                builder.append(formattingCode.ansi)
            }
            builder.append(this.message)
            builder.append(FormattingCodes.RESET.ansi)
            return builder.toString()
        }

    override val legacy: String
        get() {
            val builder = StringBuilder()
            ChatColors.getChar(color)?.let { builder.append(ProtocolDefinition.TEXT_COMPONENT_FORMATTING_PREFIX).append(it) }
            for (formattingCode in this.formatting) {
                builder.append(ProtocolDefinition.TEXT_COMPONENT_FORMATTING_PREFIX)
                builder.append(formattingCode.char)
            }
            builder.append(this.message)
            builder.append(ProtocolDefinition.TEXT_COMPONENT_FORMATTING_PREFIX).append(FormattingCodes.RESET.char) // ToDo: This should not always be appended
            return builder.toString()
        }

    override fun getJavaFXText(nodes: ObservableList<Node>): ObservableList<Node> {
        val text = Text(this.message)
        val color = this.color
        if (color == null) {
            text.styleClass += "text-default-color"
        } else {
            if (ErosProfileManager.selected.text.colored) {
                text.fill = Color.rgb(color.red, color.green, color.blue)
            }
        }
        if (FormattingCodes.OBFUSCATED in formatting) {
            // ToDo: This is just slow
            val obfuscatedTimeline = if (ErosProfileManager.selected.text.obfuscated) {
                val index = AtomicInteger()
                Timeline(
                    KeyFrame(Duration.millis(50.0), {
                        val chars = text.text.toCharArray()
                        for (i in chars.indices) {
                            chars[i] = ProtocolDefinition.OBFUSCATED_CHARS[index.getAndIncrement() % ProtocolDefinition.OBFUSCATED_CHARS.size]
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
        if (FormattingCodes.BOLD in formatting) {
            text.style += "-fx-font-weight: bold;"
        }
        if (FormattingCodes.STRIKETHROUGH in formatting) {
            text.style += "-fx-strikethrough: true;"
        }
        if (FormattingCodes.UNDERLINED in formatting) {
            text.style += "-fx-underline: true;"
        }
        if (FormattingCodes.ITALIC in formatting) {
            text.style += "-fx-font-style: italic;"
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

        for (formatting in formatting) {
            json[formatting.json] = true
        }

        color?.let { json["color"] = ChatColors.NAME_MAP.getKey(it) ?: it.toString() }

        // TODO: hover, click event

        return json
    }

    fun copy(message: Any? = this.message, color: RGBColor? = this.color, formatting: BitEnumSet<FormattingCodes> = this.formatting, clickEvent: ClickEvent? = this.clickEvent, hoverEvent: HoverEvent? = this.hoverEvent): TextComponent {
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

    override fun copy(): ChatComponent {
        return TextComponent(message, color, formatting.copy(), font, clickEvent, hoverEvent)
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
