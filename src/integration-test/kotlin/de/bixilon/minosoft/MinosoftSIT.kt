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

package de.bixilon.minosoft

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.protocol.packets.factory.PacketTypeRegistry
import org.testng.annotations.Test


internal class MinosoftSIT {

    @Test(priority = 0)
    fun disableGC() {
        Thread {
            val reference = Minosoft
            reference.hashCode()
            while (true) {
                Thread.sleep(100L)
            }
        }.start()
    }

    @Test(priority = 1)
    fun initAssetsManager() {
        Minosoft.MINOSOFT_ASSETS_MANAGER.load(CountUpAndDownLatch(0))
    }

    @Test(priority = 2)
    fun setupPacketRegistry() {
        PacketTypeRegistry.init(CountUpAndDownLatch(0))
    }

    @Test(priority = 3)
    fun loadVersionsJson() {
        Versions.load(CountUpAndDownLatch(0))
    }
}
