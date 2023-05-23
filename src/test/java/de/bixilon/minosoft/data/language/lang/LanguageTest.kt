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

package de.bixilon.minosoft.data.language.lang

import de.bixilon.minosoft.data.language.translate.Translator
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class LanguageTest {

    private fun create(placeholder: String): Translator {
        val data: LanguageData = mutableMapOf(
            KEY.path to placeholder,
        )

        return Language("test", data)
    }

    @Test
    fun none() {
        val language = create("Hello world!")
        assertEquals(language.translate(KEY)?.message, "Hello world!")
    }

    @Test
    fun args() {
        val language = create("%s %s")
        assertEquals(language.translate(KEY, data = arrayOf("hello", "world"))?.message, "hello world")
    }

    @Test
    fun numberArgs() {
        val language = create("%s %d")
        assertEquals(language.translate(KEY, data = arrayOf("hello", "world"))?.message, "hello world")
    }

    @Test
    fun textArgs() {
        val language = create("Hi %s, my name is %s and I like %s!")
        assertEquals(language.translate(KEY, data = arrayOf("Gustaf", "Moritz", "sleeping"))?.message, "Hi Gustaf, my name is Moritz and I like sleeping!")
    }

    @Test
    fun ordered() {
        val language = create("Hi %3\$s, my name is %2\$s and I like %1\$s!")
        assertEquals(language.translate(KEY, data = arrayOf("sleeping", "Moritz", "Gustaf"))?.message, "Hi Gustaf, my name is Moritz and I like sleeping!")
    }

    @Test
    fun invalid() {
        val language = create("hi %")
        assertEquals(language.translate(KEY)?.message, "hi %")
    }

    @Test
    fun invalid2() {
        val language = create("hi %   s")
        assertEquals(language.translate(KEY)?.message, "hi %   s")
    }

    @Test
    fun invalid3() {
        val language = create("hi %2$  s")
        assertEquals(language.translate(KEY)?.message, "hi %2$  s")
    }

    @Test
    fun escape() {
        val language = create("%%s %%%s %%%%s %%%%%s")
        assertEquals(language.translate(KEY)?.message, "%%s %%%s %%%%s %%%%%s")
    }

    @Test
    fun complex() {
        val language = create("Prefix, %s%3\$s again %s and %2\$s lastly %s and also %2\$s again!")
        assertEquals(language.translate(KEY, data = arrayOf("aaa", "bbb", "ccc"))?.message, "Prefix, aaaccc again bbb and bbb lastly ccc and also bbb again!")
    }

    @Test
    fun formatting() {
        val language = create("§eHi %s, welcome!")
        assertEquals(language.translate(KEY, data = arrayOf("§aMoritz"))?.legacyText, "§eHi §r§aMoritz§r§e, welcome!§r")
    }

    @Test
    fun formatting2() {
        val language = create("§eHi %s, welcome!")
        assertEquals(language.translate(KEY, data = arrayOf("§aMoritz")), BaseComponent(TextComponent("Hi ").color(ChatColors.YELLOW), TextComponent("Moritz").color(ChatColors.GREEN), TextComponent(", welcome!").color(ChatColors.YELLOW)))
    }

    @Test
    fun parent() {
        val language = create("Hi %s, welcome!")
        assertEquals(language.translate(KEY, parent = TextComponent("").color(ChatColors.YELLOW), data = arrayOf("§aMoritz"))?.legacyText, "§eHi §r§aMoritz§r§e, welcome!§r")
    }

    @Test
    fun unavailableEmpty() {
        val language = Language("test", mutableMapOf())
        assertNull(language.translate(KEY)?.message)
    }

    @Test
    fun unavailableForceEmpty() {
        val language = Language("test", mutableMapOf())
        assertEquals(language.forceTranslate(KEY).message, "minecraft:key")
    }

    @Test
    fun unavailableData() {
        val language = Language("test", mutableMapOf())
        assertEquals(language.forceTranslate(KEY, data = arrayOf("data2")).message, "minecraft:key->[data2]")
    }

    @Test
    fun fallbackData() {
        val language = Language("test", mutableMapOf())
        assertEquals(language.forceTranslate(KEY, fallback = "falling back %s!", data = arrayOf("data2")).message, "falling back data2!")
    }

    @Test
    fun trailingData() {
        val language = create("Hi %s!")
        assertEquals(language.translate(KEY, data = arrayOf("Moritz", "trail me off"))?.message, "Hi Moritz!")
    }

    @Test
    fun missingData() {
        val language = create("Hi %s %s!")
        assertEquals(language.translate(KEY, data = arrayOf("Moritz"))?.message, "Hi Moritz <null>!")
    }

    @Test
    fun tailingIndex() {
        val language = create("Hi %1\$s!!!")
        assertEquals(language.translate(KEY, data = arrayOf(null, "not me"))?.message, "Hi !!!")
    }

    @Test
    fun invalidIndex() {
        val language = create("Hi %213\$s!!!")
        assertEquals(language.translate(KEY, data = arrayOf("i am index one"))?.message, "Hi <null>!!!")
    }

    @Test
    fun recursion() {
        val language = create("Hi %s!")
        assertEquals(language.translate(KEY, data = arrayOf("hah %0\$s"))?.message, "Hi hah %0\$s!")
    }


    companion object {
        val KEY = "key".toResourceLocation()
    }
}
