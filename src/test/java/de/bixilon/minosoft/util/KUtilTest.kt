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

package de.bixilon.minosoft.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class KUtilTest {

    @Test
    fun simpleArrayModify() {
        assertEquals(4, KUtil.modifyArrayIndex(4, 5))
    }

    @Test
    fun simpleArrayModify2() {
        assertEquals(0, KUtil.modifyArrayIndex(5, 5))
    }

    @Test
    fun simpleArrayModify3() {
        assertEquals(0, KUtil.modifyArrayIndex(15, 5))
    }

    @Test
    fun negativeArrayModify() {
        assertEquals(4, KUtil.modifyArrayIndex(-1, 5))
    }

    @Test
    fun negativeArrayModify2() {
        assertEquals(4, KUtil.modifyArrayIndex(-6, 5))
    }

    @Test
    fun testInvalidSizeArrayModify() {
        assertThrows<IllegalArgumentException> { KUtil.modifyArrayIndex(0, 0) }
    }

    @Test
    fun testNonOverlappingString() {
        assertEquals(KUtil.getOverlappingText("test", "invalid"), 0)
    }

    @Test
    fun testEmptyOverlappingText() {
        assertEquals(KUtil.getOverlappingText("", ""), 0)
    }

    @Test
    fun testFullyOverlapping() {
        assertEquals(KUtil.getOverlappingText("next", "next"), 4)
    }

    @Test
    fun testSingleChatOverlapping() {
        assertEquals(KUtil.getOverlappingText("next", "test"), 1)
    }

    @Test
    fun testSingleChatOverlapping2() {
        assertEquals(KUtil.getOverlappingText("n", "nix"), 1)
    }

    @Test
    fun testSingleChatOverlapping3() {
        assertEquals(KUtil.getOverlappingText("nix", "x"), 1)
    }

    @Test
    fun testSingleChatOverlapping4() {
        assertEquals(KUtil.getOverlappingText("nix", "ix"), 2)
    }
}
