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

import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.minosoft.data.text.events.click.ClickEvent
import de.bixilon.minosoft.data.text.events.click.OpenFileClickEvent
import de.bixilon.minosoft.data.text.events.click.OpenURLClickEvent
import de.bixilon.minosoft.data.text.formatting.ChatFormattingCode
import de.bixilon.minosoft.data.text.formatting.ChatFormattingCodes
import de.bixilon.minosoft.data.text.formatting.PostChatFormattingCodes
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.io.File
import java.text.CharacterIterator
import java.text.StringCharacterIterator

private typealias PartList = MutableList<ChatComponent>

object LegacyComponentReader {


    private fun PartList.push(sequence: SequenceBuilder, restricted: Boolean) {
        if (sequence.text.isEmpty()) return

        val split = sequence.text.split(' ')

        val text = StringBuilder()

        for ((index, part) in split.withIndex()) {
            val event = getClickEvent(part, restricted)
            if (event == null) {
                text.append(part)

                if (index < split.size - 1) {
                    text.append(" ") // space was lost in the split process
                }
                continue
            }
            if (text.isNotEmpty()) {
                // an url follows, push the previous part
                this += TextComponent(text, sequence.color, sequence.formatting.toMutableSet())
                text.clear()
            }

            this += TextComponent(part, sequence.color, sequence.formatting.toMutableSet(), event)
        }
        if (text.isNotEmpty()) {
            // data that was not pushed yet
            this += TextComponent(text, sequence.color, sequence.formatting.toMutableSet())
        }

        sequence.reset()  // clear it up again for next usage
    }

    private fun getClickEvent(link: String, restricted: Boolean): ClickEvent? {
        for (protocol in URLProtocols.VALUES) {
            if (!link.startsWith(protocol.prefix)) {
                continue
            }
            if (protocol.restricted && restricted) {
                break
            }
            return if (protocol == URLProtocols.FILE) OpenFileClickEvent(File(link.removePrefix(protocol.prefix))) else OpenURLClickEvent(link.toURL())
        }
        return null
    }


    fun parse(parent: TextComponent? = null, legacy: String = "", restricted: Boolean = false): ChatComponent {
        val parts: PartList = mutableListOf()

        val sequence = SequenceBuilder(color = parent?.color, formatting = parent?.formatting?.toMutableSet() ?: mutableSetOf())

        val iterator = StringCharacterIterator(legacy)

        var char: Char
        while (true) {
            char = iterator.getAndNext()
            if (char == CharacterIterator.DONE) break

            if (char != ProtocolDefinition.TEXT_COMPONENT_FORMATTING_PREFIX) {
                sequence.text.append(char)
                continue
            }

            val formattingChar = iterator.getAndNext()

            val color = ChatColors.VALUES.getOrNull(Character.digit(formattingChar, 16))
            if (color != null) {
                parts.push(sequence, restricted) // try push previous, because this is a color change
                sequence.color = color
                continue
            }
            val formatting = ChatFormattingCodes.getChatFormattingCodeByChar(formattingChar)
            if (formatting != null) {
                parts.push(sequence, restricted) // try push previous, because this is a formatting change

                if (formatting != PostChatFormattingCodes.RESET) {
                    // a reset means resetting, this is done by the previous push
                    sequence.formatting += formatting
                }
                continue
            }
        }

        parts.push(sequence, restricted)

        return when {
            parts.isEmpty() -> EmptyComponent
            parts.size == 1 -> parts.first()
            else -> BaseComponent(parts)
        }
    }

    private fun StringCharacterIterator.getAndNext(): Char {
        val char = current()
        next()
        return char
    }

    private data class SequenceBuilder(
        var text: StringBuilder = StringBuilder(),
        var color: RGBColor? = null,
        var formatting: MutableSet<ChatFormattingCode> = mutableSetOf(),
    ) {

        fun reset() {
            text.clear()
            color = null
            formatting.clear()
        }
    }
}
