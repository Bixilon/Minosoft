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

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.gui.rendering.font.provider.FontProvider

class Font(
    val providers: MutableList<FontProvider>,
) : FontProvider {

    override fun postInit(latch: CountUpAndDownLatch) {
        val fontLatch = CountUpAndDownLatch(1, latch)
        for (provider in providers) {
            fontLatch.inc()
            DefaultThreadPool += {
                provider.postInit(latch)
                fontLatch.dec()
            }
        }
        fontLatch.dec()
        fontLatch.await()
    }

    override fun get(char: Char): CharData? {
        for (provider in providers) {
            provider[char]?.let { return it }
        }
        return null
    }


    companion object {
        const val CHAR_HEIGHT = 8
        const val CHAR_MARGIN = 1 // used for background ToDo: Set to 2, because underline does not match!
        const val TOTAL_CHAR_HEIGHT = CHAR_HEIGHT + 2 * CHAR_MARGIN // top and bottom
        const val HORIZONTAL_SPACING = 1
        const val HORIZONTAL_SPACING_SHADOW = 0
        const val HORIZONTAL_SPACING_BOLD = 1
        const val VERTICAL_SPACING = 3
    }
}
