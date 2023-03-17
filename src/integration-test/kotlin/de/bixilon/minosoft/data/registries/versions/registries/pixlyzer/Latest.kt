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

package de.bixilon.minosoft.data.registries.versions.registries.pixlyzer

import de.bixilon.minosoft.protocol.protocol.VersionSupport
import de.bixilon.minosoft.protocol.versions.VersionTypes
import de.bixilon.minosoft.protocol.versions.Versions
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["pixlyzer"], dependsOnGroups = ["version"], priority = Int.MAX_VALUE, timeOut = 15000L)
class Latest : PixLyzerLoadingTest("tba") {

    @Test(priority = -20)
    override fun loadVersion() {
        val version = Versions.getById(VersionSupport.LATEST_VERSION)!!
        if (version.type == VersionTypes.RELEASE) {
            throw SkipException("Version should already be tested!")
        }
        println("Latest version: ${version.name}")
        this._version = version
    }
}
