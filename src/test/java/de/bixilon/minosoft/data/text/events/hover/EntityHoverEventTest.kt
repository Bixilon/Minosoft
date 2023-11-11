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

package de.bixilon.minosoft.data.text.events.hover

import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.text.TextComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EntityHoverEventTest {

    @Test
    fun `1_12_2 no quotes`() {
        val data = mapOf("text" to """{name:"item.item.slimeball",id:"0d2dc333-f629-4b59-bdf9-074f58b99c06",type:"minecraft:item"}""")
        val event = EntityHoverEvent.build(data, false)

        val expected = EntityHoverEvent("0d2dc333-f629-4b59-bdf9-074f58b99c06".toUUID(), minecraft("item"), name = TextComponent("item.item.slimeball"))

        assertEquals(expected, event)
    }

    @Test
    fun `1_15_2 single quotes`() {
        val data = mapOf("text" to """{name:'{"text":"thewating"}',id:"24f0d4a2-1787-4761-aeef-39c90824e746",type:"minecraft:player"}""")
        val event = EntityHoverEvent.build(data, false)

        val expected = EntityHoverEvent("24f0d4a2-1787-4761-aeef-39c90824e746".toUUID(), minecraft("player"), name = TextComponent("thewating"))

        assertEquals(expected, event)
    }
}
