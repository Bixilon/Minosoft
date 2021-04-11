/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.text

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.hud.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.hud.elements.primitive.TextElement
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.Util
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.util.Duration


open class TextComponent(
    override var message: String = "",
    var color: RGBColor? = null,
    var formatting: MutableSet<ChatFormattingCode> = mutableSetOf(),
) : ChatComponent {

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
                stringBuilder.append(ChatColors.getANSIColorByRGBColor(it))
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
                val colorChar = ChatColors.getColorId(it)
                if (colorChar != null) {
                    stringBuilder.append(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR).append(Integer.toHexString(colorChar))
                }
            }
            for (formattingCode in this.formatting) {
                stringBuilder.append(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR)
                stringBuilder.append(formattingCode.char)
            }
            stringBuilder.append(this.message)
            stringBuilder.append(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR).append(PostChatFormattingCodes.RESET.char)
            return stringBuilder.toString()
        }

    override fun getJavaFXText(nodes: ObservableList<Node>): ObservableList<Node> {
        val text = Text(this.message)
        val color = this.color ?: ChatColors.WHITE
        text.fill = Color.WHITE
        if (Minosoft.getConfig().config.chat.colored) {
            text.fill = Color.rgb(color.red, color.green, color.blue)
        }
        for (chatFormattingCode in formatting) {
            when (chatFormattingCode) {
                PreChatFormattingCodes.OBFUSCATED -> {
                    // ToDo: potential memory leak: Stop timeline, when TextComponent isn't shown anymore
                    val obfuscatedTimeline = if (Minosoft.getConfig().config.chat.obfuscated) {
                        Timeline(KeyFrame(Duration.millis(50.0), {
                            val chars = text.text.toCharArray()
                            for (i in chars.indices) {
                                chars[i] = Util.getRandomChar(ProtocolDefinition.OBFUSCATED_CHARS)
                            }
                            text.text = String(chars)
                        }))
                    } else {
                        Timeline(KeyFrame(Duration.millis(500.0), {
                            text.isVisible = false
                        }), KeyFrame(Duration.millis(1000.0), {
                            text.isVisible = true
                        }))
                    }

                    obfuscatedTimeline.cycleCount = Animation.INDEFINITE
                    obfuscatedTimeline.play()
                    text.styleClass.add("obfuscated")
                }
                PreChatFormattingCodes.BOLD -> {
                    text.style = "-fx-font-weight: bold;"
                }
                PreChatFormattingCodes.STRIKETHROUGH -> {
                    text.style = "-fx-strikethrough: true;"
                }
                PreChatFormattingCodes.UNDERLINED -> {
                    text.style = "-fx-underline: true;"
                }
                PreChatFormattingCodes.ITALIC -> {
                    text.style = "-fx-font-weight: italic;"
                }
            }
        }
        nodes.add(text)
        return nodes
    }


    override fun prepareRender(startPosition: Vec2i, offset: Vec2i, font: Font, textElement: TextElement, z: Int, retMaxSize: Vec2i) {
        val color = this.color ?: ChatColors.WHITE


        // bring chars in right order and reverse them if right bound
        val charArray = this.message.toCharArray().toList()


        // add all chars
        for (char in charArray) {
            if (char == '\n') {
                offset.x = 0
                val yOffset = Font.CHAR_HEIGHT + RenderConstants.TEXT_LINE_PADDING
                offset.y += yOffset
                retMaxSize.y += yOffset
                continue
            }
            val fontChar = font.getChar(char)
            val scaledWidth = (fontChar.size.x * (Font.CHAR_HEIGHT / fontChar.height.toFloat())).toInt()

            val charStart = startPosition + offset
            textElement.batchAdd(ImageElement(charStart, fontChar, charStart + Vec2(scaledWidth, Font.CHAR_HEIGHT), z, color))

            // ad spacer between chars
            offset.x += scaledWidth + Font.SPACE_BETWEEN_CHARS
            if (offset.x > retMaxSize.x) {
                retMaxSize.x += scaledWidth + Font.SPACE_BETWEEN_CHARS
            }
            if (offset.y >= retMaxSize.y) {
                if (retMaxSize.y < fontChar.height) {
                    retMaxSize.y = fontChar.height
                }
            }
        }
    }
}
