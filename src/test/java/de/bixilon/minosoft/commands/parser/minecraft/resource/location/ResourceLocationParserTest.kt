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

package de.bixilon.minosoft.commands.parser.minecraft.resource.location

import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class ResourceLocationParserTest {

    @Test
    fun `read dirt`() {
        val reader = CommandReader("dirt")
        assertEquals(ResourceLocationParser.parse(reader), "minecraft:dirt".toResourceLocation())
    }

    @Test
    fun `read minecraft dirt`() {
        val reader = CommandReader("minecraft:dirt")
        assertEquals(ResourceLocationParser.parse(reader), "minecraft:dirt".toResourceLocation())
    }

    @Test
    fun readEmpty() {
        val reader = CommandReader("")
        assertThrows<InvalidResourceLocationError> { ResourceLocationParser.parse(reader) }
    }
}
