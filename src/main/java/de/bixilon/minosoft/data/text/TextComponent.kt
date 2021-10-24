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
package de.bixilon.minosoft.data.text

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.text.events.ClickEvent
import de.bixilon.minosoft.data.text.events.HoverEvent
import de.bixilon.minosoft.gui.eros.dialog.ErosErrorReport.Companion.report
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.hyperlink
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import de.bixilon.minosoft.util.Util
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
    override val formatting: MutableSet<ChatFormattingCode> = DEFAULT_FORMATTING.toMutableSet(),
    var clickEvent: ClickEvent? = null,
    var hoverEvent: HoverEvent? = null,
) : ChatComponent, TextStyle {
    override var message: String = message?.toString()?.replace(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR, '&') ?: "null"

    fun obfuscate(): TextComponent {
        formatting.add(PreChatFormattingCodes.OBFUSCATED)
        return this
    }

    fun bold(): TextComponent {
        formatting.add(PreChatFormattingCodes.BOLD)
        return this
    }

    fun strikethrough(): TextComponent {
        formatting.add(PreChatFormattingCodes.STRIKETHROUGH)
        return this
    }

    fun underline(): TextComponent {
        formatting.add(PreChatFormattingCodes.UNDERLINED)
        return this
    }

    fun italic(): TextComponent {
        formatting.add(PreChatFormattingCodes.ITALIC)
        return this
    }

    fun shadow(): TextComponent {
        formatting.add(PreChatFormattingCodes.SHADOWED)
        return this
    }

    fun clickEvent(clickEvent: ClickEvent?) {
        this.clickEvent = clickEvent
    }

    fun hoverEvent(hoverEvent: HoverEvent?) {
        this.hoverEvent = hoverEvent
    }

    fun color(color: RGBColor): TextComponent {
        this.color = color
        return this
    }

    override fun toString(): String {
        return legacyText
    }

    override fun applyDefaultColor(color: RGBColor) {
        if (this.color == null) {
            this.color = color
        }
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
                    stringBuilder.append(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR).append(Integer.toHexString(colorChar))
                }
            }
            for (formattingCode in this.formatting) {
                stringBuilder.append(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR)
                stringBuilder.append(formattingCode.char)
            }
            stringBuilder.append(this.message)
            stringBuilder.append(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR).append(PostChatFormattingCodes.RESET.char) // ToDo: This should not always be appended
            return stringBuilder.toString()
        }

    override fun getJavaFXText(nodes: ObservableList<Node>): ObservableList<Node> {
        val text = Text(this.message)
        this.color?.let {
            if (Minosoft.config.config.chat.colored) {
                text.fill = Color.rgb(it.red, it.green, it.blue)
            }
        } ?: let {
            text.styleClass += "text-default-color"
        }
        for (chatFormattingCode in formatting) {
            when (chatFormattingCode) {
                PreChatFormattingCodes.OBFUSCATED -> {
                    // ToDo: potential memory/performance leak: Stop timeline, when TextComponent isn't shown anymore
                    val obfuscatedTimeline = if (Minosoft.config.config.chat.obfuscated) {
                        Timeline(
                            KeyFrame(Duration.millis(50.0), {
                                val chars = text.text.toCharArray()
                                for (i in chars.indices) {
                                    chars[i] = Util.getRandomChar(ProtocolDefinition.OBFUSCATED_CHARS)
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

        clickEvent?.let { event ->
            when (event.action) {
                ClickEvent.ClickEventActions.OPEN_URL -> text.hyperlink(event.value.toString())
                else -> {
                    NotImplementedError("Unknown action ${event.action}").report()
                    return@let
                }
            }
        }

        hoverEvent?.let {
            when (it.action) {
                HoverEvent.HoverEventActions.SHOW_TEXT -> text.accessibleText = it.value.toString() // ToDo
                else -> {
                    NotImplementedError("Unknown action ${it.action}").report()
                    return@let
                }
            }
        }
        return nodes
    }

    fun copy(message: Any? = this.message, color: RGBColor? = this.color, formatting: MutableSet<ChatFormattingCode> = this.formatting.toSynchronizedSet(), clickEvent: ClickEvent? = this.clickEvent, hoverEvent: HoverEvent? = this.hoverEvent): TextComponent {
        return TextComponent(
            message = message,
            color = color,
            formatting = formatting,
            clickEvent = clickEvent,
            hoverEvent = hoverEvent
        )
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


    companion object {
        val DEFAULT_FORMATTING: Set<ChatFormattingCode> = setOf(PreChatFormattingCodes.SHADOWED)
    }
}
