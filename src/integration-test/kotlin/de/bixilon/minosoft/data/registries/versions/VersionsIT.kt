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

package de.bixilon.minosoft.data.registries.versions

import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["version"])
internal class VersionsIT {

    @Test
    fun test1_7() {
        assertEquals(ProtocolVersions.V_1_7_10, Versions["1.7.10"]!!.versionId)
    }

    @Test
    fun test1_8() {
        assertEquals(ProtocolVersions.V_1_8_9, Versions["1.8.9"]!!.versionId)
    }

    @Test
    fun test1_9() {
        assertEquals(ProtocolVersions.V_1_9_4, Versions["1.9.4"]!!.versionId)
    }

    @Test
    fun test1_10() {
        assertEquals(ProtocolVersions.V_1_10_2, Versions["1.10.2"]!!.versionId)
    }

    @Test
    fun test1_11() {
        assertEquals(ProtocolVersions.V_1_11_2, Versions["1.11.2"]!!.versionId)
    }

    @Test
    fun test1_12() {
        assertEquals(ProtocolVersions.V_1_12_2, Versions["1.12.2"]!!.versionId)
    }

    @Test
    fun test1_13() {
        assertEquals(ProtocolVersions.V_1_13_2, Versions["1.13.2"]!!.versionId)
    }

    @Test
    fun test1_14() {
        assertEquals(ProtocolVersions.V_1_14, Versions["1.14"]!!.versionId)
    }

    @Test
    fun test1_15() {
        assertEquals(ProtocolVersions.V_1_15, Versions["1.15"]!!.versionId)
    }

    @Test
    fun test1_16() {
        assertEquals(ProtocolVersions.V_1_16, Versions["1.16"]!!.versionId)
    }

    @Test
    fun test1_17() {
        assertEquals(ProtocolVersions.V_1_17_1, Versions["1.17.1"]!!.versionId)
    }

    @Test
    fun test1_18() {
        assertEquals(ProtocolVersions.V_1_18_2, Versions["1.18.2"]!!.versionId)
    }

    @Test
    fun test1_19() {
        assertEquals(ProtocolVersions.V_1_19_2, Versions["1.19.2"]!!.versionId)
    }
}
