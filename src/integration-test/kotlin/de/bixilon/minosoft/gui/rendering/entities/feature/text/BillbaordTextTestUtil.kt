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

package de.bixilon.minosoft.gui.rendering.entities.feature.text

object BillbaordTextTestUtil {

    fun BillboardTextFeature.assertText() {
        val text = this.text
        if (text == null || text.length == 0) throw AssertionError("Text is empty but should not be!")
    }

    fun BillboardTextFeature.assertEmpty() {
        val text = this.text ?: return
        if (text.length == 0) return
        throw AssertionError("Text is empty but should not be!")
    }
}
