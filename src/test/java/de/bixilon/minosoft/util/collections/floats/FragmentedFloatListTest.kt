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

package de.bixilon.minosoft.util.collections.floats

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.primitive.floats.FloatList
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import org.junit.jupiter.api.Test
import java.nio.FloatBuffer
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class FragmentedFloatListTest : DirectFloatListTest() {

    override fun create(initialSize: Int) = FragmentedFloatList(initialSize)


    private fun FloatList.putMixed() {
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
        val list = FragmentedFloatList(1000)
        for (i in 0 until 2000) {
            list.putMixed()
        }
        assertEquals(14000, list.size)
        val array = list.toArray()
        assertEquals(14000, array.size)
        val buffer = list.toBuffer()
        assertEquals(14000, buffer.position())
        for (i in array.indices) {
            val expected = (i % 7 + 1).toFloat()
            assertEquals(expected, array[i])
            assertEquals(expected, buffer[i])
        }
        assertEquals(14, list._complete.size)
        assertEquals(0, list._incomplete.size)
    }

    @Test
    fun `batch adding 7 floats`() {
        val list = FragmentedFloatList(1)
        for (i in 0 until 100) {
            val offset = i * 7.0f
            list.add(offset + 0, offset + 1, offset + 2, offset + 3, offset + 4, offset + 5, offset + 6)
        }
        assertEquals(list.size, 700)
        val expected = FloatArray(700) { it.toFloat() }
        val array = list.toArray()
        assertContentEquals(expected, array)
    }

    @Test
    fun `batch adding 7 floats and ensuring size`() {
        val list = FragmentedFloatList(100)
        for (i in 0 until 100) {
            list.ensureSize(21)
            val offset = i * 7.0f
            list.add(offset + 0, offset + 1, offset + 2, offset + 3, offset + 4, offset + 5, offset + 6)
        }
        assertEquals(list.size, 700)
        val expected = FloatArray(700) { it.toFloat() }
        val array = list.toArray()
        assertContentEquals(expected, array)
    }

    @Test
    fun `batch adding float array`() {
        val list = FragmentedFloatList(1)
        for (i in 0 until 100) {
            val offset = i * 7.0f
            list += floatArrayOf(offset + 0, offset + 1, offset + 2, offset + 3, offset + 4, offset + 5, offset + 6)
        }
        assertEquals(list.size, 700)
        val expected = FloatArray(700) { it.toFloat() }
        val array = list.toArray()
        assertContentEquals(expected, array)
    }

    private val FragmentedFloatList._complete get() = FragmentedFloatList::class.java.getFieldOrNull("complete")!!.get(this).unsafeCast<List<FloatBuffer>>()
    private val FragmentedFloatList._incomplete get() = FragmentedFloatList::class.java.getFieldOrNull("incomplete")!!.get(this).unsafeCast<List<FloatBuffer>>()
}
