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

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FragmentedFloatListTest : DirectFloatListTest() {

    override fun create(initialSize: Int): AbstractFloatList {
        return FragmentedArrayFloatList(initialSize)
    }


    private fun AbstractFloatList.putMixed() {
        ensureSize(7)
        add(1.0f)
        add(2.0f)
        add(floatArrayOf(3.0f, 4.0f))
        add(5.0f)
        add(6.0f)
        add(floatArrayOf(7.0f))
    }

    @Test
    fun testMixed() {
        val list = FragmentedArrayFloatList(1000)
        for (i in 0 until 2000) {
            println(i)
            list.putMixed()
        }
        assertEquals(14000, list.size)
        assertEquals(14000, list.toArray().size)
        assertEquals(14, list.fragments.size)
    }
}
