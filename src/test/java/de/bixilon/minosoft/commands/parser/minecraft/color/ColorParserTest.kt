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

package de.bixilon.minosoft.commands.parser.minecraft.color

import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


internal class ColorParserTest {

    @Test
    fun testYellow() {
        val reader = CommandReader("yellow")
        val parser = ColorParser()
        assertEquals(parser.parse(reader), ChatColors.YELLOW)
    }

    @Test
    fun testGreen() {
        val reader = CommandReader("green")
        val parser = ColorParser()
        assertEquals(parser.parse(reader), ChatColors.GREEN)
    }

    @Test
    fun testInvalidColor() {
        val reader = CommandReader("invalid")
        val parser = ColorParser()
        assertThrows<ColorParseError> { parser.parse(reader) }
    }

    @Test
    fun testHexBlack() {
        val reader = CommandReader("#000000")
        val parser = ColorParser()
        assertEquals(parser.parse(reader), ChatColors.BLACK)
    }

    @Test
    fun testHexWhite() {
        val reader = CommandReader("#FFFFFF")
        val parser = ColorParser()
        assertEquals(parser.parse(reader), ChatColors.WHITE)
    }

    @Test
    fun testInvalidHex() {
        val reader = CommandReader("#FFIFFF")
        val parser = ColorParser()
        assertThrows<ColorParseError> { parser.parse(reader) }
    }

    @Test
    fun testHexDisabled() {
        val reader = CommandReader("#ABCDEF")
        val parser = ColorParser(allowRGB = false)
        assertThrows<HexNotSupportedError> { parser.parse(reader) }
    }

    @Test
    fun testYellowSuggestions() {
        val reader = CommandReader("y")
        val parser = ColorParser()
        assertEquals(parser.getSuggestions(reader), listOf(Suggestion(0, "yellow", TextComponent("yellow").color(ChatColors.YELLOW))))
    }

    @Test
    fun darkSuggestion() {
        val reader = CommandReader("dark")
        val parser = ColorParser()
        assertEquals(parser.getSuggestions(reader).toSet(), setOf(
            Suggestion(0, "dark_blue", TextComponent("dark_blue").color(ChatColors.DARK_BLUE)),
            Suggestion(0, "dark_green", TextComponent("dark_green").color(ChatColors.DARK_GREEN)),
            Suggestion(0, "dark_aqua", TextComponent("dark_aqua").color(ChatColors.DARK_AQUA)),
            Suggestion(0, "dark_red", TextComponent("dark_red").color(ChatColors.DARK_RED)),
            Suggestion(0, "dark_purple", TextComponent("dark_purple").color(ChatColors.DARK_PURPLE)),
            Suggestion(0, "dark_gray", TextComponent("dark_gray").color(ChatColors.DARK_GRAY)),
        ))
    }

    @Test
    fun testHexSuggestion() {
        val reader = CommandReader("#")
        val parser = ColorParser()
        assertEquals(parser.getSuggestions(reader), emptyList())
    }

    @Test
    fun testInvalidColorSuggestion() {
        val reader = CommandReader("none")
        val parser = ColorParser()
        assertThrows<ColorParseError> { parser.getSuggestions(reader) }
    }
}
