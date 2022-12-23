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

package de.bixilon.minosoft.gui.rendering

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.assets.AssetsLoader
import de.bixilon.minosoft.gui.rendering.font.FontLoader
import de.bixilon.minosoft.gui.rendering.font.provider.BitmapFontProvider
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test

@Test(priority = 100, groups = ["rendering"])
class RenderTestLoader {

    fun init() {
        val connection = createConnection(5)
        val latch = CountUpAndDownLatch(1)
        connection::assetsManager.forceSet(AssetsLoader.create(connection.profiles.resources, connection.version, latch))
        FontLoader.remove(BitmapFontProvider) // TODO: remove
        connection.assetsManager.load(latch)
        RenderTestUtil.rendering = Rendering(connection)
        RenderTestUtil.rendering.start(latch, audio = false)
        latch.dec()
        latch.await()
        RenderTestUtil.context = RenderTestUtil.rendering.renderWindow
    }
}
