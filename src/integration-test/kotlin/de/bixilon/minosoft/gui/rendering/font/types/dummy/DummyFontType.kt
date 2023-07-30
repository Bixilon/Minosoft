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

package de.bixilon.minosoft.gui.rendering.font.types.dummy

import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.types.FontType

object DummyFontType : FontType {
    private val chars: Array<DummyCodePointRenderer?> = arrayOfNulls(26) // a-z

    // a:0 b:0.5 c:1.0 d:1.5 e:2.0 f:2.5 g:3.0 h:3.5

    init {
        build()
    }

    fun build() {
        for (i in 0 until chars.size) {
            chars[i] = DummyCodePointRenderer(width = i / 2.0f)
        }
    }

    override fun get(codePoint: Int): CodePointRenderer? {
        if (codePoint in 'a'.code..'z'.code) {
            return chars[codePoint - 'a'.code]
        }
        return null
    }
}
