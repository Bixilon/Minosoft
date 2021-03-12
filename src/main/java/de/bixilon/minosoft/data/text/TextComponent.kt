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
import de.bixilon.minosoft.util.hash.BetterHashSet
import glm_.vec2.Vec2
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.util.Duration
import java.util.*
import java.util.function.Consumer


open class TextComponent : ChatComponent {
    private val text: String
    var color: RGBColor? = null
        private set
    var formatting: BetterHashSet<ChatFormattingCode> = BetterHashSet()

    constructor(text: String, color: RGBColor?, formatting: BetterHashSet<ChatFormattingCode>) {
        this.text = text
        this.color = color
        this.formatting = formatting
    }

    constructor(text: String, color: RGBColor?) {
        this.text = text
        this.color = color
    }

    constructor(text: String) {
        this.text = text
    }

    fun setObfuscated(obfuscated: Boolean): TextComponent {
        formatting.addOrRemove(PreChatFormattingCodes.OBFUSCATED, obfuscated)
        return this
    }

    fun setBold(bold: Boolean): TextComponent {
        formatting.addOrRemove(PreChatFormattingCodes.BOLD, bold)
        return this
    }

    fun setStrikethrough(strikethrough: Boolean): TextComponent {
        formatting.addOrRemove(PreChatFormattingCodes.STRIKETHROUGH, strikethrough)
        return this
    }

    fun setUnderlined(underlined: Boolean): TextComponent {
        formatting.addOrRemove(PreChatFormattingCodes.UNDERLINED, underlined)
        return this
    }

    fun setItalic(italic: Boolean): TextComponent {
        formatting.addOrRemove(PreChatFormattingCodes.ITALIC, italic)
        return this
    }

    fun setReset(reset: Boolean): TextComponent {
        formatting.addOrRemove(PostChatFormattingCodes.RESET, reset)
        return this
    }

    fun setColor(color: RGBColor?): TextComponent {
        this.color = color
        return this
    }

    fun setFormatting(formatting: BetterHashSet<ChatFormattingCode>): TextComponent {
        this.formatting = formatting
        return this
    }

    override fun hashCode(): Int {
        return Objects.hash(text, color, formatting)
    }

    override fun equals(other: Any?): Boolean {
        if (super.equals(other)) {
            return true
        }
        if (hashCode() != other.hashCode()) {
            return false
        }
        val their = other as TextComponent?
        return text == their!!.message && color == their.color && formatting == their.formatting
    }

    override fun toString(): String {
        return legacyText
    }

    override fun getANSIColoredMessage(): String {
        val builder = StringBuilder()
        this.color?.let {
            builder.append(ChatColors.getANSIColorByRGBColor(it))
        }

        for (formattingCode in this.formatting) {
            if (formattingCode is PreChatFormattingCodes) {
                builder.append(formattingCode.getANSI())
            }
        }
        builder.append(this.text)
        for (formattingCode in this.formatting) {
            if (formattingCode is PostChatFormattingCodes) {
                builder.append(formattingCode.getANSI())
            }
        }
        builder.append(PostChatFormattingCodes.RESET)
        return builder.toString()
    }

    override fun getLegacyText(): String {
        val output = StringBuilder()
        if (color != null) {
            val colorChar = ChatColors.getColorId(color)
            if (colorChar != null) {
                output.append(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR).append(Integer.toHexString(colorChar))
            }
        }
        formatting.forEach(Consumer { chatFormattingCode: ChatFormattingCode -> output.append(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR).append(chatFormattingCode.char) })
        output.append(text)
        output.append(ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR).append(PostChatFormattingCodes.RESET.char)
        return output.toString()
    }

    override fun getMessage(): String {
        return text
    }

    override fun getJavaFXText(nodes: ObservableList<Node>): ObservableList<Node> {
        val text = Text(text)
        val color = this.color ?: ProtocolDefinition.DEFAULT_COLOR
        text.fill = Color.WHITE
        if (Minosoft.getConfig().config.chat.colored) {
            text.fill = Color.rgb(color.red, color.green, color.blue)
        }
        for (chatFormattingCode in formatting) {
            if (chatFormattingCode is PreChatFormattingCodes) {
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
        }
        nodes.add(text)
        return nodes
    }

    override fun prepareRender(startPosition: Vec2, offset: Vec2, font: Font, textElement: TextElement, z: Int, retMaxSize: Vec2) {
        val color = this.color ?: ProtocolDefinition.DEFAULT_COLOR


        // bring chars in right order and reverse them if right bound
        val charArray = text.toCharArray().toList()


        // add all chars
        for (char in charArray) {
            if (char == '\n') {
                val yOffset = offset.y
                offset *= 0
                offset += Vec2(0, yOffset + Font.CHAR_HEIGHT + RenderConstants.TEXT_LINE_PADDING)
                retMaxSize += Vec2(0, yOffset + Font.CHAR_HEIGHT + RenderConstants.TEXT_LINE_PADDING)
                continue
            }
            val fontChar = font.getChar(char)
            val scaledWidth = fontChar.width * (Font.CHAR_HEIGHT / fontChar.height.toFloat())

            val charStart = startPosition + offset
            textElement.addChild(ImageElement(charStart, fontChar, charStart + Vec2(scaledWidth, Font.CHAR_HEIGHT), z, color))

            // ad spacer between chars
            offset += Vec2(scaledWidth + Font.SPACE_BETWEEN_CHARS, 0f)
            if (offset.x > retMaxSize.x) {
                retMaxSize.x += scaledWidth + Font.SPACE_BETWEEN_CHARS
            }
            if (offset.y >= retMaxSize.y) {
                if (retMaxSize.y < fontChar.height) {
                    retMaxSize.y = fontChar.height.toFloat()
                }
            }
        }
    }
}
