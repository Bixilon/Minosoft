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

package de.bixilon.minosoft.data.language.placeholder

import de.bixilon.kutil.array.ArrayUtil.isIndex
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.EmptyComponent
import de.bixilon.minosoft.data.text.TextComponent

object PlaceholderUtil {
    private val DIGIT_RANGE = '0'.code..'9'.code
    private const val ESCAPE = '%'.code
    private const val INDEX = '$'.code
    private const val STRING = 's'.code
    private const val DIGIT = 'd'.code

    private fun PlaceholderIteratorOptions.processEscape() {
        if (escaped) {
            builder.appendCodePoint(ESCAPE)
        } else {
            escaped = true
        }
    }

    private fun PlaceholderIteratorOptions.push() {
        if (builder.isEmpty()) return
        val text = ChatComponent.of(builder.toString(), parent = previous ?: parent, restricted = restricted)
        if (text is TextComponent) {
            previous = text
        }
        component += text
        builder.clear()
    }

    private fun PlaceholderIteratorOptions.appendArgument(index: Int) {
        val value = if (data.isIndex(index)) data[index] else "<null>"
        component += ChatComponent.of(value, parent = previous ?: parent, restricted = restricted)
    }

    private fun PlaceholderIteratorOptions.processOrdered() {
        push()
        appendArgument(dataIndex++)
    }

    private fun PlaceholderIteratorOptions.processIndexed(char: Int) {
        if (char !in DIGIT_RANGE) {
            return processChar(char)
        }
        val indexBuilder = StringBuilder()

        indexBuilder.appendCodePoint(char)

        var trailing = 0
        while (iterator.hasNext()) {
            val digit = iterator.nextInt()
            if (digit in DIGIT_RANGE) {
                indexBuilder.appendCodePoint(digit)
            } else {
                trailing = digit
                break
            }
        }
        if (trailing != INDEX) {
            indexBuilder.append(trailing)
        }
        if (!iterator.hasNext()) {
            builder.append(indexBuilder)
            return
        }
        val type = iterator.nextInt()
        if (trailing != INDEX || (type != STRING && type != DIGIT)) {
            builder.append(indexBuilder)
            return
        }

        push()
        appendArgument(Integer.parseInt(indexBuilder.toString()) - 1)
    }

    private fun PlaceholderIteratorOptions.processChar() = processChar(iterator.nextInt())
    private fun PlaceholderIteratorOptions.processChar(char: Int) {
        if (char == ESCAPE) {
            return processEscape()
        }
        if (!escaped) {
            builder.appendCodePoint(char)
            return
        }
        escaped = false
        if (char != STRING && char != DIGIT) {
            return processIndexed(char)
        }

        return processOrdered()
    }


    fun format(placeholder: String, parent: TextComponent? = null, restricted: Boolean = false, vararg data: Any?): ChatComponent {
        if (data.isEmpty()) return ChatComponent.of(placeholder, parent = parent, restricted = restricted)


        val options = PlaceholderIteratorOptions(placeholder.codePoints().iterator(), parent, restricted, data)

        while (options.iterator.hasNext()) {
            options.processChar()
        }
        options.push()

        return options.component.trim() ?: EmptyComponent
    }
}
