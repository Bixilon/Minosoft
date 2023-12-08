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

package de.bixilon.minosoft.protocol.packets.s2c.play.tab

import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.data.entities.entities.player.additional.AdditionalDataUpdate
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.protocol.packets.s2c.play.PacketReadingTestUtil
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["packet"])
class TabListS2CPTest {

    fun `greev_eu 1_15_2`() {
        val packet = PacketReadingTestUtil.read("tab/greev_eu_1_15_2", "1.15.2", constructor = ::TabListS2CP)
        assertEquals(packet.entries["24f0d4a2-1787-4761-aeef-39c90824e746".toUUID()], AdditionalDataUpdate(hasDisplayName = true, displayName = TextComponent(" thewating ").color(ChatColors.GREEN)))
    }

    fun `greev_eu 1_20_2`() {
        val packet = PacketReadingTestUtil.read("tab/greev_eu_1_20_2", "1.20.2", constructor = ::TabListS2CP)
        assertEquals(packet.entries["24f0d4a2-1787-4761-aeef-39c90824e746".toUUID()], AdditionalDataUpdate(hasDisplayName = true, displayName = TextComponent(" thewating ").color(ChatColors.GREEN)))
    }

    fun `greev_eu 1_20_4`() {
        val packet = PacketReadingTestUtil.read("tab/greev_eu_1_20_4", "1.20.4", constructor = ::TabListS2CP)
        assertEquals(packet.entries["24f0d4a2-1787-4761-aeef-39c90824e746".toUUID()], AdditionalDataUpdate(hasDisplayName = true, displayName = TextComponent(" thewating ").color(ChatColors.GREEN)))
    }
}

