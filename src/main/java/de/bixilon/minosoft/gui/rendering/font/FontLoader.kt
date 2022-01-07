/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.font

import de.bixilon.kutil.check.CheckUtil.check
import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.provider.BitmapFontProvider
import de.bixilon.minosoft.gui.rendering.font.provider.FontProvider
import de.bixilon.minosoft.gui.rendering.font.provider.FontProviderFactory
import de.bixilon.minosoft.gui.rendering.font.provider.LegacyUnicodeFontProvider
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast

object FontLoader : DefaultFactory<FontProviderFactory<*>>(
    BitmapFontProvider,
    LegacyUnicodeFontProvider,
    // ToDo: True type font
) {
    private val FONT_INDEX = "font/default.json".toResourceLocation()


    fun load(renderWindow: RenderWindow, latch: CountUpAndDownLatch): Font {
        val fontIndex = renderWindow.connection.assetsManager[FONT_INDEX].readJsonObject()

        val providers: MutableList<FontProvider> = synchronizedListOf()

        val fontLatch = CountUpAndDownLatch(1, latch)
        for (provider in fontIndex["providers"].listCast<Map<String, Any>>()!!) {
            val type = provider["type"].toResourceLocation()
            fontLatch.inc()
            DefaultThreadPool += {
                providers += this[type].check { "Unknown font provider type $type" }.build(renderWindow, provider)
                fontLatch.dec()
            }
        }
        fontLatch.dec()
        fontLatch.await()

        return Font(
            providers = providers,
        )
    }
}
