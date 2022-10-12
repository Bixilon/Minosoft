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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DNSUtilTest {

    @Test
    fun fixInvalid1() {
        assertEquals("bixilon.de", DNSUtil.fixAddress("bixilon.de"))
    }

    @Test
    fun fixInvalid2() {
        assertEquals("bixilon.de", DNSUtil.fixAddress(" bixilon.de"))
    }

    @Test
    fun fixInvalid3() {
        assertEquals("bixilon.de", DNSUtil.fixAddress("bixilon,de"))
    }

    @Test
    fun fixInvalid4() {
        assertEquals("bixilon.de", DNSUtil.fixAddress("http://bixilon.de"))
    }

    @Test
    fun fixInvalid5() {
        assertEquals("bixilon.de", DNSUtil.fixAddress("https://bixilon.de"))
    }

    @Test
    fun fixInvalid6() {
        assertEquals("bixilon.de", DNSUtil.fixAddress("https://bixilon.de/"))
    }

    @Test
    fun fixInvalid7() {
        assertEquals("bixilon.de", DNSUtil.fixAddress("\"bixilon.de\""))
    }

    @Test
    fun fixInvalid8() {
        assertEquals("bixilon.de", DNSUtil.fixAddress(":bixilon.de"))
    }

    @Test
    fun fixInvalid9() {
        assertEquals("bixilon.de", DNSUtil.fixAddress("HTTPS://bixilon.de"))
    }

    @Test
    fun fixInvalid10() {
        assertEquals("bixilon.de", DNSUtil.fixAddress("https://bixilon,de/"))
    }
}
