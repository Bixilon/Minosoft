/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.minosoft.data.text.ChatComponent.Companion.chat
import de.bixilon.minosoft.data.text.RGBColor.Companion.asColor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class ChatComponentTest {


    @Test
    fun testEmpty() {
        assertSame(ChatComponent.EMPTY, "".chat())
    }

    @Test
    fun testSimpleText() {
        val expected = BaseComponent(parts = arrayOf(TextComponent("Test")))
        val actual = "Test".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testSimpleColor() {
        val expected = BaseComponent(parts = arrayOf(TextComponent("Test").color(ChatColors.RED)))
        val actual = "§cTest".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testSimpleColoredFormatting() {
        val expected = BaseComponent(parts = arrayOf(TextComponent("Test").color(ChatColors.RED).strikethrough()))
        val actual = "§c§mTest".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testSwappedSimpleColoredFormatting() {
        val expected = BaseComponent(parts = arrayOf(TextComponent("Test").color(ChatColors.RED).strikethrough()))
        val actual = "§m§cTest".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun test2Texts() {
        val expected = BaseComponent(
            TextComponent("Test").color(ChatColors.RED),
            TextComponent("Next").color(ChatColors.GREEN),
        )
        val actual = "§cTest§aNext".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun test2FormattedTexts() {
        val expected = BaseComponent(
            TextComponent("Test").color(ChatColors.RED).italic(),
            TextComponent("Next").color(ChatColors.GREEN).italic(),
        )
        val actual = "§c§oTest§a§oNext".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testSimpleJsonReading() {
        val expected = BaseComponent(
            parts = arrayOf(
                TextComponent("Test"),
            )
        )
        val actual = """{"text":"Test"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testColorJsonReading() {
        val expected = BaseComponent(
            parts = arrayOf(
                TextComponent("Test").color(ChatColors.YELLOW),
            )
        )
        val actual = """{"text":"Test", "color": "yellow"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testHexColor() {
        val expected = BaseComponent(
            parts = arrayOf(
                TextComponent("Test").color("#123456".asColor()),
            )
        )
        val actual = """{"text":"Test", "color": "#123456"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testJsonArray() {
        val expected = BaseComponent(
            parts = arrayOf(
                BaseComponent(
                    parts = arrayOf(
                        TextComponent("Test"),
                    )
                )
            )
        )
        val actual = """[{"text":"Test"}]""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testJsonWithWhitespaces() {
        val expected = BaseComponent(
            parts = arrayOf(
                TextComponent("Test"),
            )
        )
        val actual = """    {"text":"Test"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testInvalidJson() {
        val expected = BaseComponent(
            parts = arrayOf(
                TextComponent("""{text":"Test"}"""),
            )
        )
        val actual = """{text":"Test"}""".chat()
        assertEquals(expected, actual)
    }
}
