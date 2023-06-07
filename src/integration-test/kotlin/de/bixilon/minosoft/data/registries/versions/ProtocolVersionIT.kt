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

package de.bixilon.minosoft.data.registries.versions

import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.versions.Versions
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["version"])
internal class ProtocolVersionIT {

    @Test
    fun `1_7_10`() {
        assertEquals(ProtocolVersions.V_1_7_10, Versions["1.7.10"]!!.versionId)
    }

    @Test
    fun `1_8_9`() {
        assertEquals(ProtocolVersions.V_1_8_9, Versions["1.8.9"]!!.versionId)
    }

    @Test
    fun `1_9_1`() {
        assertEquals(ProtocolVersions.V_1_9_1, Versions["1.9.1"]!!.versionId)
    }

    @Test
    fun `1_9_4`() {
        assertEquals(ProtocolVersions.V_1_9_4, Versions["1.9.4"]!!.versionId)
    }

    @Test
    fun `1_10_2`() {
        assertEquals(ProtocolVersions.V_1_10_2, Versions["1.10.2"]!!.versionId)
    }

    @Test
    fun `1_11_2`() {
        assertEquals(ProtocolVersions.V_1_11_2, Versions["1.11.2"]!!.versionId)
    }

    @Test
    fun `1_12`() {
        assertEquals(ProtocolVersions.V_1_12, Versions["1.12"]!!.versionId)
    }

    @Test
    fun `1_12_1`() {
        assertEquals(ProtocolVersions.V_1_12_1, Versions["1.12.1"]!!.versionId)
    }

    @Test
    fun `1_12_2`() {
        assertEquals(ProtocolVersions.V_1_12_2, Versions["1.12.2"]!!.versionId)
    }

    @Test
    fun `1_13`() {
        assertEquals(ProtocolVersions.V_1_13, Versions["1.13"]!!.versionId)
    }

    @Test
    fun `1_13_1`() {
        assertEquals(ProtocolVersions.V_1_13_1, Versions["1.13.1"]!!.versionId)
    }

    @Test
    fun `1_13_2`() {
        assertEquals(ProtocolVersions.V_1_13, Versions["1.13"]!!.versionId)
    }

    @Test
    fun `1_14`() {
        assertEquals(ProtocolVersions.V_1_14, Versions["1.14"]!!.versionId)
    }

    @Test
    fun `1_14_1`() {
        assertEquals(ProtocolVersions.V_1_14_1, Versions["1.14.1"]!!.versionId)
    }

    @Test
    fun `1_14_2`() {
        assertEquals(ProtocolVersions.V_1_14_2, Versions["1.14.2"]!!.versionId)
    }

    @Test
    fun `1_14_3`() {
        assertEquals(ProtocolVersions.V_1_14_3, Versions["1.14.3"]!!.versionId)
    }

    @Test
    fun `1_14_4`() {
        assertEquals(ProtocolVersions.V_1_14_4, Versions["1.14.4"]!!.versionId)
    }

    @Test
    fun `1_15`() {
        assertEquals(ProtocolVersions.V_1_15, Versions["1.15"]!!.versionId)
    }

    @Test
    fun `1_15_1`() {
        assertEquals(ProtocolVersions.V_1_15_1, Versions["1.15.1"]!!.versionId)
    }

    @Test
    fun `1_15_2`() {
        assertEquals(ProtocolVersions.V_1_15_2, Versions["1.15.2"]!!.versionId)
    }

    @Test
    fun `1_16`() {
        assertEquals(ProtocolVersions.V_1_16, Versions["1.16"]!!.versionId)
    }

    @Test
    fun `1_16_1`() {
        assertEquals(ProtocolVersions.V_1_16_1, Versions["1.16.1"]!!.versionId)
    }

    @Test
    fun `1_16_2`() {
        assertEquals(ProtocolVersions.V_1_16_2, Versions["1.16.2"]!!.versionId)
    }

    @Test
    fun `1_16_3`() {
        assertEquals(ProtocolVersions.V_1_16_3, Versions["1.16.3"]!!.versionId)
    }

    @Test
    fun `1_16_5`() {
        assertEquals(ProtocolVersions.V_1_16_5, Versions["1.16.5"]!!.versionId)
    }

    @Test
    fun `1_17`() {
        assertEquals(ProtocolVersions.V_1_17, Versions["1.17"]!!.versionId)
    }

    @Test
    fun `1_17_1`() {
        assertEquals(ProtocolVersions.V_1_17_1, Versions["1.17.1"]!!.versionId)
    }

    @Test
    fun `1_18_1`() {
        assertEquals(ProtocolVersions.V_1_18_1, Versions["1.18.1"]!!.versionId)
    }

    @Test
    fun `1_18_2`() {
        assertEquals(ProtocolVersions.V_1_18_2, Versions["1.18.2"]!!.versionId)
    }

    @Test
    fun `1_19`() {
        assertEquals(ProtocolVersions.V_1_19, Versions["1.19"]!!.versionId)
    }

    @Test
    fun `1_19_2`() {
        assertEquals(ProtocolVersions.V_1_19_2, Versions["1.19.2"]!!.versionId)
    }

    @Test
    fun `1_19_3`() {
        assertEquals(ProtocolVersions.V_1_19_3, Versions["1.19.3"]!!.versionId)
    }

    @Test
    fun `1_19_4`() {
        assertEquals(ProtocolVersions.V_1_19_4, Versions["1.19.4"]!!.versionId)
    }

    @Test
    fun `1_20`() {
        assertEquals(ProtocolVersions.V_1_20, Versions["1.20"]!!.versionId)
    }
}
