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

package de.bixilon.minosoft.data.entities.entities.player.additional

import de.bixilon.minosoft.data.scoreboard.team.Team
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import org.testng.Assert.assertEquals
import org.testng.Assert.assertSame
import org.testng.annotations.Test

@Test(groups = ["scoreboard"])
class PlayerAdditionalTest {

    fun `tab name when custom name set`() {
        val additional = PlayerAdditional("Me")
        val name = TextComponent("hehe")
        additional.displayName = name
        assertSame(additional.tabDisplayName, name) // TODO: verify
    }

    fun `tab name without team`() {
        val additional = PlayerAdditional("Me")
        assertEquals(additional.tabDisplayName, TextComponent("Me"))
    }

    fun `team prefix`() {
        val additional = PlayerAdditional("Me")
        additional.team = Team("test", prefix = TextComponent("[P]").color(ChatColors.RED))
        assertEquals(additional.tabDisplayName, BaseComponent(TextComponent("[P]").color(ChatColors.RED), TextComponent("Me")))
    }

    fun `team suffix`() {
        val additional = PlayerAdditional("Me")
        additional.team = Team("test", suffix = TextComponent("[S]").color(ChatColors.BLUE))
        assertEquals(additional.tabDisplayName, BaseComponent(TextComponent("Me"), TextComponent("[S]").color(ChatColors.BLUE)))
    }

    fun `team prefix and suffix`() {
        val additional = PlayerAdditional("Me")
        additional.team = Team("test", prefix = TextComponent("[P]").color(ChatColors.RED), suffix = TextComponent("[S]").color(ChatColors.BLUE))
        assertEquals(additional.tabDisplayName, BaseComponent(TextComponent("[P]").color(ChatColors.RED), TextComponent("Me"), TextComponent("[S]").color(ChatColors.BLUE)))
    }

    fun `team prefix and suffix and color`() {
        val additional = PlayerAdditional("Me")
        additional.team = Team("test", color = ChatColors.LIGHT_PURPLE, prefix = TextComponent("[P]").color(ChatColors.RED), suffix = TextComponent("[S]").color(ChatColors.BLUE))
        assertEquals(additional.tabDisplayName, BaseComponent(TextComponent("[P]").color(ChatColors.RED), TextComponent("Me").color(ChatColors.LIGHT_PURPLE), TextComponent("[S]").color(ChatColors.BLUE)))
    }

    // TODO: comparing
}
