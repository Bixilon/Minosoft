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

package de.bixilon.minosoft.gui.eros.util.text

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.EmptyComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.FormattingCodes
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.util.Duration
import java.util.concurrent.atomic.AtomicInteger

interface JavaFXTextRenderer<C> {

    fun render(nodes: MutableList<Node>, text: C)


    object BaseComponentRenderer : JavaFXTextRenderer<BaseComponent> {
        override fun render(nodes: MutableList<Node>, text: BaseComponent) {
            for (part in text.parts) {
                render(nodes, part)
            }
        }
    }

    object TextComponentRenderer : JavaFXTextRenderer<TextComponent> {
        override fun render(nodes: MutableList<Node>, text: TextComponent) {
            val node = Text(text.message)
            val color = text.color
            if (color == null) {
                node.styleClass += "text-default-color"
            } else {
                if (ErosProfileManager.selected.text.colored) {
                    node.fill = Color.rgb(color.red, color.green, color.blue)
                }
            }
            if (FormattingCodes.OBFUSCATED in text.formatting) {
                // ToDo: This is just slow
                val obfuscatedTimeline = if (ErosProfileManager.selected.text.obfuscated) {
                    val index = AtomicInteger()
                    Timeline(
                        KeyFrame(Duration.millis(50.0), {
                            val chars = node.text.toCharArray()
                            for (i in chars.indices) {
                                chars[i] = ProtocolDefinition.OBFUSCATED_CHARS[index.getAndIncrement() % ProtocolDefinition.OBFUSCATED_CHARS.size]
                            }
                            node.text = String(chars)
                        }),
                    )
                } else {
                    Timeline(
                        KeyFrame(Duration.millis(500.0), {
                            node.isVisible = false
                        }),
                        KeyFrame(Duration.millis(1000.0), {
                            node.isVisible = true
                        }),
                    )
                }

                obfuscatedTimeline.cycleCount = Animation.INDEFINITE
                obfuscatedTimeline.play()
                node.styleClass.add("obfuscated")
            }
            if (FormattingCodes.BOLD in text.formatting) {
                node.style += "-fx-font-weight: bold;"
            }
            if (FormattingCodes.STRIKETHROUGH in text.formatting) {
                node.style += "-fx-strikethrough: true;"
            }
            if (FormattingCodes.UNDERLINED in text.formatting) {
                node.style += "-fx-underline: true;"
            }
            if (FormattingCodes.ITALIC in text.formatting) {
                node.style += "-fx-font-style: italic;"
            }
            nodes.add(node)

            text.clickEvent?.applyJavaFX(node)
            text.hoverEvent?.applyJavaFX(node)
        }
    }

    companion object : JavaFXTextRenderer<ChatComponent> {

        fun render(text: ChatComponent): MutableList<Node> {
            val nodes: MutableList<Node> = mutableListOf()
            render(nodes, text)

            return nodes
        }

        override fun render(nodes: MutableList<Node>, text: ChatComponent) = when (text) {
            is EmptyComponent -> Unit
            is BaseComponent -> BaseComponentRenderer.render(nodes, text)
            is TextComponent -> TextComponentRenderer.render(nodes, text)
            else -> Log.log(LogMessageType.OTHER, LogLevels.WARN) { "Can not render $text" }
        }
    }
}
