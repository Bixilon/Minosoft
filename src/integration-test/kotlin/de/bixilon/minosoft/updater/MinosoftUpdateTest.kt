/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.updater

import de.bixilon.kutil.array.ByteArrayUtil.toHex
import de.bixilon.kutil.hash.HashUtil.sha512
import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.properties.MinosoftP
import de.bixilon.minosoft.properties.MinosoftProperties
import de.bixilon.minosoft.properties.general.GeneralP
import org.testng.Assert.assertThrows
import org.testng.annotations.Test


@Test(groups = ["updater"])
class MinosoftUpdateTest {

    init {
        MinosoftProperties = MinosoftP(GeneralP("old", -10L, false, false), null)
    }

    fun `no download link`() {
        MinosoftUpdate("dummy", "Dummy version", MinosoftProperties.general.date + 1, true, null, null, ChatComponent.of(":)"))
    }

    fun `correct data`() {
        MinosoftUpdate("dummy", "Dummy version", MinosoftProperties.general.date + 1, true, null, DownloadLink("https://bixilon.de/secret-update.jar".toURL(), 123, ByteArray(1).sha512().toHex(), ""), ChatComponent.of(":)"))
    }

    fun `older signature`() {
        assertThrows { MinosoftUpdate("dummy", "Dummy version", MinosoftProperties.general.date, true, null, DownloadLink("https://bixilon.de/secret-update.jar".toURL(), 123, ByteArray(1).sha512().toHex(), ""), ChatComponent.of(":)")) }
    }
}
