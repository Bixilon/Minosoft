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

package de.bixilon.minosoft.gui.rendering.util.mesh.builder

import de.bixilon.kutil.collections.primitive.ints.IntList

object IndexUtil {

    fun addIndexQuad(list: IntList, front: Boolean, reverse: Boolean) {
        val size = list.size
        if (front) {
            list.add(size + 0, size + 1, size + 2, size + 3, size + 4, size + 5)
        }
        if (reverse) {
            list.add(size + 5, size + 4, size + 3, size + 2, size + 1, size + 0)
        }
    }
}
