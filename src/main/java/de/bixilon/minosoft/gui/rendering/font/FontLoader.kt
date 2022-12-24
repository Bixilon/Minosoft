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

package de.bixilon.minosoft.gui.rendering.font

import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.data.registries.factory.DefaultFactory
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.provider.*
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.trim
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast

object FontLoader : DefaultFactory<FontProviderFactory<*>>(
    BitmapFontProvider,
    LegacyUnicodeFontProvider,
    SpaceFontProvider,
    // ToDo: True type font
) {
    private val FONT_INDEX = "font/default.json".toResourceLocation()


    fun load(context: RenderContext, latch: CountUpAndDownLatch): Font {
        val fontIndex = context.connection.assetsManager.getOrNull(FONT_INDEX)?.readJsonObject() ?: return Font(arrayOf())

        val providersRaw = fontIndex["providers"].listCast<Map<String, Any>>()!!
        val providers: Array<FontProvider?> = arrayOfNulls(providersRaw.size)

        val worker = UnconditionalWorker()
        for ((index, provider) in providersRaw.withIndex()) {
            val type = provider["type"].toResourceLocation()
            worker += add@{
                val factory = this[type]
                if (factory == null) {
                    Log.log(LogMessageType.RENDERING_LOADING, LogLevels.WARN) { "Unknown font provider: $type" }
                    return@add
                }
                providers[index] = factory.build(context, provider)
            }
        }
        worker.work(latch)

        return Font(
            providers = providers.trim(),
        )
    }
}
