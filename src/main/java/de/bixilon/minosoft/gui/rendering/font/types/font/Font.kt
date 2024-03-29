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

package de.bixilon.minosoft.gui.rendering.font.types.font

import de.bixilon.kutil.array.ArrayUtil.isIndex
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.ParentLatch
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.types.FontType
import de.bixilon.minosoft.gui.rendering.font.types.PostInitFontType

class Font(
    val providers: Array<FontType>,
) : PostInitFontType {
    private val cache: Array<CodePointRenderer?> = arrayOfNulls(CACHE_SIZE)

    init {
        buildCache()
    }


    private fun postInitFonts(latch: AbstractLatch) {
        val fontLatch = ParentLatch(1, latch)
        for (provider in providers) {
            if (provider !is PostInitFontType) continue
            fontLatch.inc()
            DefaultThreadPool += {
                provider.postInit(latch)
                fontLatch.dec()
            }
        }
        fontLatch.dec()
        fontLatch.await()
    }

    private fun buildCache() {
        for (char in cache.indices) {
            cache[char] = forceGet(char + CACHE_START)
        }
    }

    override fun postInit(latch: AbstractLatch) {
        postInitFonts(latch)
    }

    fun forceGet(codePoint: Int): CodePointRenderer? {
        for (provider in providers) {
            provider[codePoint]?.let { return it }
        }
        return null
    }

    override fun get(codePoint: Int): CodePointRenderer? {
        val cacheIndex = codePoint - CACHE_START
        if (cache.isIndex(cacheIndex)) {
            return cache[cacheIndex]
        }
        return forceGet(codePoint)
    }


    fun trim(): FontType? {
        return when (providers.size) {
            0 -> null
            1 -> providers.first()
            else -> this
        }
    }


    companion object {
        private const val CACHE_START = ' '.code // all control chars (like ESC, CR, ...) are not used anyway (in a normal environment)
        private const val CACHE_SIZE = (1 shl 7) - CACHE_START  // ascii
    }
}
