/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.data.language.lang.LanguageFile
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.text.ChatComponent.Companion.chat
import de.bixilon.minosoft.data.text.events.click.OpenFileClickEvent
import de.bixilon.minosoft.data.text.events.click.OpenURLClickEvent
import de.bixilon.minosoft.data.text.events.click.SendMessageClickEvent
import de.bixilon.minosoft.data.text.events.click.SuggestChatClickEvent
import de.bixilon.minosoft.data.text.events.hover.EntityHoverEvent
import de.bixilon.minosoft.data.text.events.hover.TextHoverEvent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.rgb
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class ChatComponentTest {

    @Test
    fun testEmpty() {
        assertSame(ChatComponent.EMPTY, "".chat())
    }

    @Test
    fun testSimpleText() {
        val expected = TextComponent("Test")
        val actual = "Test".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testSimpleColor() {
        val expected = TextComponent("Test").color(ChatColors.RED)
        val actual = "§cTest".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testSimpleColoredFormatting() {
        val expected = TextComponent("Test").color(ChatColors.RED).strikethrough()
        val actual = "§c§mTest".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testSwappedSimpleColoredFormatting() {
        val expected = TextComponent("Test").color(ChatColors.RED).strikethrough()
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
    fun url1() {
        val expected = BaseComponent(
            TextComponent("Test").color(ChatColors.RED),
            TextComponent("https://bixilon.de").color(ChatColors.GREEN).clickEvent(OpenURLClickEvent("https://bixilon.de".toURL())),
        )
        val actual = "§cTest§ahttps://bixilon.de".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun url2() {
        val expected = BaseComponent(
            TextComponent("Test ").color(ChatColors.RED),
            TextComponent("file:/home/moritz").color(ChatColors.GREEN).clickEvent(OpenFileClickEvent("/home/moritz")),
        )
        val actual = ChatComponent.of("§cTest §afile:/home/moritz")
        assertEquals(expected, actual)
    }

    @Test
    fun url3() {
        val expected = BaseComponent(
            TextComponent("Hi, please take care of: "),
            TextComponent("https://bixilon.de/technoblade").clickEvent(OpenURLClickEvent("https://bixilon.de/technoblade".toURL())),
        )
        val actual = ChatComponent.of("Hi, please take care of: https://bixilon.de/technoblade")
        assertEquals(expected, actual)
    }

    @Test
    fun restrictedMode() {
        val expected = BaseComponent(
            TextComponent("Test ").color(ChatColors.RED),
            TextComponent("file:/home/moritz").color(ChatColors.GREEN),
        )
        val actual = ChatComponent.of("§cTest §afile:/home/moritz", restricted = true)
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
        val expected = TextComponent("Test")
        val actual = """{"text":"Test"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testColorJsonReading() {
        val expected = TextComponent("Test").color(ChatColors.YELLOW)
        val actual = """{"text":"Test", "color": "yellow"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testHexColor() {
        val expected = TextComponent("Test").color("#123456".rgb())
        val actual = """{"text":"Test", "color": "#123456"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testJsonArray() {
        val expected = TextComponent("Test")
        val actual = """[{"text":"Test"}]""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testJsonWithWhitespaces() {
        val expected = TextComponent("Test")
        val actual = """    {"text":"Test"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testInvalidJson() {
        val expected = TextComponent("""{text":"Test"}""")
        val actual = """{text":"Test"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun cuberiteChatJson() {
        val expected = BaseComponent(TextComponent("<"), TextComponent("Bixilon"), TextComponent("> "), TextComponent("hello"))
        val actual = """{"extra":[{"text":"<"},{"text":"Bixilon"},{"text":"> "},{"text":"hello"}],"text":""}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testJson1() {
        val text = TextComponent("dummy")
        assertEquals(text.toJson(), mapOf("text" to "dummy"))
    }

    @Test
    fun testJson2() {
        val text = ChatComponent.of("dummy")
        assertEquals(text.toJson(), mapOf("text" to "dummy"))
    }

    @Test
    fun testJson3() {
        val text = ChatComponent.of("dummy§knext")
        assertEquals(text.toJson(), listOf(mapOf("text" to "dummy"), mapOf("text" to "next", "obfuscated" to true)))
    }

    @Test
    fun testJson4() {
        val text = ChatComponent.of("dummy§anext")
        assertEquals(text.toJson(), listOf(mapOf("text" to "dummy"), mapOf("text" to "next", "color" to "green")))
    }

    @Test
    fun hypixelMotd() {                                                                     // ↓ Note that extra paragraph
        val string = "                §aHypixel Network §c[1.8-1.19]\n   §c§lLUNAR MAPS §7§l§ §6§lCOSMETICS §7| §d§lSKYBLOCK 0.17.3"
        val component = ChatComponent.of(string)

        val expected = BaseComponent(
            "                ",
            TextComponent("Hypixel Network ").color(ChatColors.GREEN),
            TextComponent("[1.8-1.19]\n   ").color(ChatColors.RED),
            TextComponent("LUNAR MAPS ").color(ChatColors.RED).bold(),
            TextComponent("COSMETICS ").color(ChatColors.GOLD).bold(),
            TextComponent("| ").color(ChatColors.GRAY),
            TextComponent("SKYBLOCK 0.17.3").color(ChatColors.LIGHT_PURPLE).bold(),
        )


        assertEquals("                Hypixel Network [1.8-1.19]\n   LUNAR MAPS COSMETICS | SKYBLOCK 0.17.3", component.message)
        assertEquals(expected, component)
    }

    @Test
    fun levelingReward() {
        val string = """{"text":"§eClick here to view it!","strikethrough":false,"clickEvent":{"action":"run_command","value":"/rewards"},"hoverEvent":{"action":"show_text","value":{"text":"Click to open the §3Hypixel Leveling §emenu","color":"yellow","strikethrough":false}}}"""
        val component = ChatComponent.of(string)

        val expected = TextComponent("Click here to view it!")
            .color(ChatColors.YELLOW)
            .clickEvent(SendMessageClickEvent("/rewards"))
            .hoverEvent(
                TextHoverEvent(BaseComponent(
                    TextComponent("Click to open the ").color(ChatColors.YELLOW),
                    TextComponent("Hypixel Leveling ").color(ChatColors.DARK_AQUA),
                    TextComponent("menu").color(ChatColors.YELLOW),
                )))


        assertEquals(expected, component)
    }

    @Test
    fun `JSON not escaped new line`() {
        val text = ChatComponent.of("""{"text":"Unsupported protocol version 762.""" + "\n" + """Try connecting with Minecraft 1.8.x-1.12.x"}""")
        val expected = TextComponent("Unsupported protocol version 762.\nTry connecting with Minecraft 1.8.x-1.12.x")
        assertEquals(text, expected)
    }

    @Test
    fun `Nested translations`() {
        val language = LanguageFile("en_US", Namespaces.MINECRAFT, mutableMapOf(
            "gameMode.changed" to "Dein Spielmodus wurde zu %s geändert",
            "gameMode.creative" to "Kreativmodus",
        ))
        val text = ChatComponent.of("""{"translate":"gameMode.changed","with":[{"translate":"gameMode.creative"}]}""", translator = language)
        val expected = BaseComponent(TextComponent("Dein Spielmodus wurde zu "), TextComponent("Kreativmodus"), TextComponent(" geändert"))
        assertEquals(text, expected)
    }

    @Test
    fun `inserted translations`() {
        val language = LanguageFile("en_US", Namespaces.MINECRAFT, mutableMapOf(
            "death.attack.generic" to "%1\$s starb",
        ))
        val text = ChatComponent.of("""{"translate":"death.attack.generic","with":[{"color":"light_purple","insertion":"Bixilon","clickEvent":{"action":"suggest_command","value":"/tell Bixilon "},"hoverEvent":{"action":"show_entity","contents":{"type":"minecraft:player","id":"1d410d09-750b-3200-993c-47f31b30baf0","name":{"text":"Bixilon"}}},"extra":[{"bold":true,"color":"green","text":"[Admin] "},{"text":"Bixilon"}],"text":""}]}""", translator = language)

        val click = SuggestChatClickEvent("/tell Bixilon ")
        val hover = EntityHoverEvent("1d410d09-750b-3200-993c-47f31b30baf0".toUUID(), minecraft("player"), name = TextComponent("Bixilon"))

        val expected = BaseComponent(
            BaseComponent(
                TextComponent("[Admin] ").color(ChatColors.GREEN).bold().clickEvent(click).hoverEvent(hover),
                TextComponent("Bixilon").color(ChatColors.LIGHT_PURPLE).clickEvent(click).hoverEvent(hover),
            ), TextComponent(" starb"))
        assertEquals(text, expected)
    }

    @Test
    fun `nested translation with legacy entity event`() {
        val language = LanguageFile("en_US", Namespaces.MINECRAFT, mutableMapOf(
            "chat.type.admin" to "[%s: %s]",
            "commands.kill.successful" to "Killed %s",
        ))
        val text = ChatComponent.of("""{"italic":true,"color":"gray","translate":"chat.type.admin","with":["Bixilon",{"translate":"commands.kill.successful","with":[{"insertion":"0d2dc333-f629-4b59-bdf9-074f58b99c06","hoverEvent":{"action":"show_entity","value":{"text":"{name:\"item.item.slimeball\",id:\"0d2dc333-f629-4b59-bdf9-074f58b99c06\",type:\"minecraft:item\"}"}},"text":"item.item.slimeball"}]}]}""", translator = language)

        val hover = EntityHoverEvent("0d2dc333-f629-4b59-bdf9-074f58b99c06".toUUID(), minecraft("item"), name = TextComponent("item.item.slimeball"))

        val expected = BaseComponent(
            TextComponent("[").color(ChatColors.GRAY).italic(),
            TextComponent("Bixilon").color(ChatColors.GRAY).italic(),
            TextComponent(": ").color(ChatColors.GRAY).italic(),
            BaseComponent(
                TextComponent("Killed ").color(ChatColors.GRAY).italic(),
                TextComponent("item.item.slimeball").color(ChatColors.GRAY).italic().hoverEvent(hover),
            ),
            TextComponent("]").color(ChatColors.GRAY).italic(),
        )
        assertEquals(text, expected)
    }

    @Test
    fun `nbt solo text`() {
        val language = LanguageFile("en_US", Namespaces.MINECRAFT, mutableMapOf(
            "chat.type.admin" to "[%s: %s]"
        ))
        val text = ChatComponent.of("""{"with":[{"color":"red","extra":[{"color":"red","text":"[Admins] "},{"":"Bixilon"},{"":""}],"insertion":"Bixilon"," text":""},{"text":"test"}],"color":"gray","italic":1,"translate":"chat.type.admin"}""", translator = language)
        val expected = BaseComponent(
            TextComponent("[").color(ChatColors.GRAY).italic(),
            BaseComponent(
                TextComponent("[Admins] ").color(ChatColors.RED).italic(),
                TextComponent("Bixilon").color(ChatColors.RED).italic(),
            ),
            TextComponent(": ").color(ChatColors.GRAY).italic(),
            TextComponent("test").color(ChatColors.GRAY).italic(),
            TextComponent("]").color(ChatColors.GRAY).italic(),
        )
        assertEquals(text, expected)
    }

    @Test
    fun `remove quotes around legacy text`() { // tree.ac
        val string = """"§2Join the Other Server? Find it at §6Port 25566§2!""""
        val text = ChatComponent.of(string)

        val expected = BaseComponent(
            TextComponent("Join the Other Server? Find it at ").color(ChatColors.DARK_GREEN),
            TextComponent("Port 25566").color(ChatColors.GOLD),
            TextComponent("!").color(ChatColors.DARK_GREEN),
        )
        assertEquals(text, expected)
    }

    private fun assertEquals(expected: ChatComponent, actual: ChatComponent) {
        when (expected) {
            is BaseComponent -> {
                if (actual !is BaseComponent) assert("Type mismatch", "BaseComponent", actual::class.java.name)

                if (expected.parts.size != actual.parts.size) assert("Count of parts does not match", expected.parts, actual.parts)

                for (index in expected.parts.indices) {
                    val first = expected.parts[index]
                    val second = actual.parts[index]

                    assertEquals(first, second)
                }
            }

            is TextComponent -> {
                if (actual !is TextComponent) assert("Type mismatch", "TextComponent", actual::class.java.name)
                if (expected.message != actual.message) assert("Message mismatch", expected.message, actual.message)
                if (expected.clickEvent != actual.clickEvent) assert("Click event mismatch: $expected", expected.clickEvent, actual.clickEvent)
                if (expected.hoverEvent != actual.hoverEvent) assert("Click event mismatch: $expected", expected.hoverEvent, actual.hoverEvent)
                if (expected.color != actual.color) assert("Color mismatch", expected.color, actual.color)
                if (expected.font != actual.font) assert("Font mismatch: $expected", expected.font, actual.font)
                if (expected.formatting != actual.formatting) assert("Formatting mismatch: $expected", expected.formatting, actual.formatting)

                assertEquals(expected as Any, actual)
            }

            else -> assertEquals(expected as Any, actual)
        }
    }

    private fun assert(message: String, expected: Any?, actual: Any?): Nothing = throw AssertionFailedError("$message: expected=$expected, actual=$actual", expected, actual)
}
