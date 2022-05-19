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

package de.bixilon.minosoft.commands.parser.minecraft.target

import de.bixilon.minosoft.commands.parser.minecraft.target.targets.identifier.name.InvalidNameError
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.identifier.name.NameEntityTarget
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.identifier.uuid.InvalidUUIDError
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.identifier.uuid.UUIDEntityTarget
import de.bixilon.minosoft.commands.util.CommandReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals


internal class TargetParserTest {

    @Test
    fun testName() {
        val reader = CommandReader("Bixilon")
        val parser = TargetParser()
        assertEquals(parser.parse(reader), NameEntityTarget("Bixilon"))
    }

    @Test
    fun testInvalidName() {
        val reader = CommandReader("Bi§ilon")
        val parser = TargetParser()
        assertThrows<InvalidNameError> { parser.parse(reader) }
    }


    @Test
    fun testUUID() {
        val reader = CommandReader("9e6ce7c5-40d3-483e-8e5a-b6350987d65f")
        val parser = TargetParser()
        assertEquals(parser.parse(reader), UUIDEntityTarget(UUID.fromString("9e6ce7c5-40d3-483e-8e5a-b6350987d65f")))
    }

    @Test
    fun testTrimmedUUID() {
        val reader = CommandReader("9e6ce7c540d3483e8e5ab6350987d65f")
        val parser = TargetParser()
        assertEquals(parser.parse(reader), UUIDEntityTarget(UUID.fromString("9e6ce7c5-40d3-483e-8e5a-b6350987d65f")))
    }

    @Test
    fun testInvalidUUID() {
        val reader = CommandReader("9g6ce7c540d3483e8e5ab6350987d65f")
        val parser = TargetParser()
        assertThrows<InvalidUUIDError> { parser.parse(reader) }
    }

    @Test
    fun testTooShortUUID() {
        val reader = CommandReader("9e6ce7c540d3483e8e5ab63")
        val parser = TargetParser()
        assertThrows<InvalidUUIDError> { parser.parse(reader) }
    }

    @Test
    fun testTooLongUUID() {
        val reader = CommandReader("9e6ce7c540d3483e8e5ab6350987d65f123")
        val parser = TargetParser()
        assertThrows<InvalidUUIDError> { parser.parse(reader) }
    }
}
