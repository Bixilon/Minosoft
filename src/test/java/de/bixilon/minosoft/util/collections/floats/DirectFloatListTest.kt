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

package de.bixilon.minosoft.util.collections.floats

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class DirectFloatListTest : AbstractFloatListTest() {
    @Test
    fun singleToBuffer() {
        val list = create()
        if (list !is DirectArrayFloatList) {
            return
        }
        list.add(189.0f)
        val buffer = list.toBuffer()
        Assertions.assertEquals(buffer.get(0), 189.0f)
        Assertions.assertEquals(buffer.position(), 1)
    }

    @Test
    fun multipleToBuffer() {
        val list = create()
        if (list !is DirectArrayFloatList) {
            return
        }
        list.add(189.0f)
        list.add(289.0f)
        val buffer = list.toBuffer()
        Assertions.assertEquals(buffer.get(0), 189.0f)
        Assertions.assertEquals(buffer.get(1), 289.0f)
        Assertions.assertEquals(buffer.position(), 2)
    }
}
