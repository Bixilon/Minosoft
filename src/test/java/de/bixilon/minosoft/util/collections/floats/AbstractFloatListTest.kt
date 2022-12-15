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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.FloatBuffer
import kotlin.test.assertContentEquals

abstract class AbstractFloatListTest {

    abstract fun create(initialSize: Int = 1): AbstractFloatList

    @Test
    fun initialListSize() {
        val list = create()
        assertEquals(0, list.size)
    }

    @Test
    fun addSingleFloat() {
        val list = create()
        list.add(189.0f)
        assertEquals(1, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f), array)
    }

    @Test
    fun addMultipleFloats() {
        val list = create()
        list.add(189.0f)
        list.add(289.0f)
        list.add(389.0f)
        assertEquals(3, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f, 289.0f, 389.0f), array)
    }

    @Test
    fun addVastFloats() {
        val list = create()
        for (i in 0 until 1000) {
            list.add(i.toFloat() * 2.0f)
        }
        assertEquals(1000, list.size)
        val array = list.toArray()
        for (i in 0 until 1000) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun addSingleArray() {
        val list = create()
        list.add(floatArrayOf(189.0f))
        assertEquals(1, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f), array)
    }

    @Test
    fun addMultipleArrays() {
        val list = create()
        list.add(floatArrayOf(189.0f))
        list.add(floatArrayOf(289.0f))
        list.add(floatArrayOf(389.0f))
        assertEquals(3, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f, 289.0f, 389.0f), array)
    }

    @Test
    fun addMultipleBigArrays() {
        val list = create()
        list.add(floatArrayOf(189.0f))
        list.add(floatArrayOf(289.0f, 389.0f))
        list.add(floatArrayOf(489.0f, 589.0f, 689.0f))
        assertEquals(6, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f, 289.0f, 389.0f, 489.0f, 589.0f, 689.0f), array)
    }

    @Test
    fun addVastArrays() {
        val list = create()

        for (i in 0 until 1000) {
            list.add(floatArrayOf(i.toFloat() * 2.0f))
        }
        assertEquals(1000, list.size)
        val array = list.toArray()
        for (i in 0 until 1000) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun addSingleBuffer() {
        val list = create()
        list.add(wrap(189.0f))
        assertEquals(1, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f), array)
    }

    @Test
    fun addMultipleBuffers() {
        val list = create()
        list.add(wrap(189.0f))
        list.add(wrap(289.0f))
        list.add(wrap(389.0f))
        assertEquals(3, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f, 289.0f, 389.0f), array)
    }

    @Test
    fun addMultipleBigBuffers() {
        val list = create()
        list.add(wrap(189.0f))
        list.add(wrap(289.0f, 389.0f))
        list.add(wrap(489.0f, 589.0f, 689.0f))
        assertEquals(6, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f, 289.0f, 389.0f, 489.0f, 589.0f, 689.0f), array)
    }

    @Test
    fun addVastBuffers() {
        val list = create()

        for (i in 0 until 1000) {
            list.add(wrap(i.toFloat() * 2.0f))
        }
        assertEquals(1000, list.size)
        val array = list.toArray()
        for (i in 0 until 1000) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }


    @Test
    fun addSingleHeapList() {
        val list = create()
        list.add(HeapArrayFloatList(1).apply { add(189.0f) })
        assertEquals(1, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f), array)
    }

    @Test
    fun addMultipleHeapLists() {
        val list = create()
        list.add(HeapArrayFloatList(1).apply { add(189.0f) })
        list.add(HeapArrayFloatList(1).apply { add(289.0f) })
        list.add(HeapArrayFloatList(1).apply { add(389.0f) })
        assertEquals(3, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f, 289.0f, 389.0f), array)
    }

    @Test
    fun addMultipleBigHeapLists() {
        val list = create()
        list.add(HeapArrayFloatList(1).apply { add(189.0f) })
        list.add(HeapArrayFloatList(1).apply { add(289.0f); add(389.0f) })
        list.add(HeapArrayFloatList(1).apply { add(489.0f); add(589.0f); add(689.0f) })

        assertEquals(6, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f, 289.0f, 389.0f, 489.0f, 589.0f, 689.0f), array)
    }

    @Test
    fun addVastHeapLists() {
        val list = create()

        for (i in 0 until 1000) {
            list.add(HeapArrayFloatList(1).apply { add(i.toFloat() * 2.0f) })
        }
        assertEquals(1000, list.size)
        val array = list.toArray()
        for (i in 0 until 1000) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }


    @Test
    fun addSingleBufferedList() {
        val list = create()
        list.add(BufferedArrayFloatList(1).apply { add(189.0f) })
        assertEquals(1, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f), array)
    }

    @Test
    fun addMultipleBufferedLists() {
        val list = create()
        list.add(BufferedArrayFloatList(1).apply { add(189.0f) })
        list.add(BufferedArrayFloatList(1).apply { add(289.0f) })
        list.add(BufferedArrayFloatList(1).apply { add(389.0f) })
        assertEquals(3, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f, 289.0f, 389.0f), array)
    }

    @Test
    fun addMultipleBigBufferedLists() {
        val list = create()
        list.add(BufferedArrayFloatList(1).apply { add(189.0f) })
        list.add(BufferedArrayFloatList(1).apply { add(289.0f); add(389.0f) })
        list.add(BufferedArrayFloatList(1).apply { add(489.0f); add(589.0f); add(689.0f) })

        assertEquals(6, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f, 289.0f, 389.0f, 489.0f, 589.0f, 689.0f), array)
    }

    @Test
    fun addVastBufferedLists() {
        val list = create()

        for (i in 0 until 1000) {
            list.add(BufferedArrayFloatList(1).apply { add(i.toFloat() * 2.0f) })
        }
        assertEquals(1000, list.size)
        val array = list.toArray()
        for (i in 0 until 1000) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun addSingleFragmentedList() {
        val list = create()
        list.add(FragmentedArrayFloatList(1).apply { add(189.0f) })
        assertEquals(1, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f), array)
    }

    @Test
    fun addMultipleFragmentedLists() {
        val list = create()
        list.add(FragmentedArrayFloatList(1).apply { add(189.0f) })
        list.add(FragmentedArrayFloatList(1).apply { add(289.0f) })
        list.add(FragmentedArrayFloatList(1).apply { add(389.0f) })
        assertEquals(3, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f, 289.0f, 389.0f), array)
    }

    @Test
    fun addMultipleBigFragmentedLists() {
        val list = create()
        list.add(FragmentedArrayFloatList(1).apply { add(189.0f) })
        list.add(FragmentedArrayFloatList(1).apply { add(289.0f); add(389.0f) })
        list.add(FragmentedArrayFloatList(1).apply { add(489.0f); add(589.0f); add(689.0f) })

        assertEquals(6, list.size)
        val array = list.toArray()
        assertContentEquals(floatArrayOf(189.0f, 289.0f, 389.0f, 489.0f, 589.0f, 689.0f), array)
    }

    @Test
    fun addVastFragmentedLists() {
        val list = create()

        for (i in 0 until 1000) {
            list.add(FragmentedArrayFloatList(1).apply { add(i.toFloat() * 2.0f) })
        }
        assertEquals(1000, list.size)
        val array = list.toArray()
        for (i in 0 until 1000) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun fragmentedArray() {
        val list = create(120)
        list.add(FloatArray(110) { it.toFloat() * 2 })
        list.add(FloatArray(50) { 220 + it.toFloat() * 2 })
        list.add(320.0f)

        val array = list.toArray()
        for (i in 0 until 161) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun fragmentedBuffer() {
        val list = create(120)
        list.add(wrap(*FloatArray(110) { it.toFloat() * 2 }))
        list.add(wrap(*FloatArray(50) { 220 + it.toFloat() * 2 }))
        list.add(320.0f)

        val array = list.toArray()
        for (i in 0 until 161) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun fragmentedHeapList() {
        val list = create(120)
        list.add(HeapArrayFloatList().apply { add(FloatArray(110) { it.toFloat() * 2 }) })
        list.add(HeapArrayFloatList().apply { add(FloatArray(50) { 220 + it.toFloat() * 2 }) })
        list.add(320.0f)

        val array = list.toArray()
        for (i in 0 until 161) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun fragmentedBufferedList() {
        val list = create(120)
        list.add(BufferedArrayFloatList().apply { add(FloatArray(110) { it.toFloat() * 2 }) })
        list.add(BufferedArrayFloatList().apply { add(FloatArray(50) { 220 + it.toFloat() * 2 }) })
        list.add(320.0f)

        val array = list.toArray()
        for (i in 0 until 161) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun fragmentedFragmentedList() {
        val list = create(120)
        list.add(FragmentedArrayFloatList().apply { add(FloatArray(110) { it.toFloat() * 2 }) })
        list.add(FragmentedArrayFloatList().apply { add(FloatArray(50) { 220 + it.toFloat() * 2 }) })
        list.add(320.0f)

        val array = list.toArray()
        for (i in 0 until 161) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun fragmentedFragmentedList2() {
        val list = create(120)
        list.add(FragmentedArrayFloatList(10).apply { add(FloatArray(110) { it.toFloat() * 2 }) })
        list.add(FragmentedArrayFloatList(14).apply { add(FloatArray(50) { 220 + it.toFloat() * 2 }) })
        list.add(320.0f)

        val array = list.toArray()
        for (i in 0 until 161) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }


    @Test
    fun ensureSizeArray() {
        val list = create(120)
        list.add(0.0f)
        list.ensureSize(1000)
        list.ensureSize(2000)
        list.add(FloatArray(1000) { 2.0f + it.toFloat() * 2 })

        val array = list.toArray()
        for (i in 0 until 1001) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun ensureSizeBuffer() {
        val list = create(120)
        list.add(0.0f)
        list.ensureSize(1000)
        list.ensureSize(2000)
        list.add(wrap(*FloatArray(1000) { 2.0f + it.toFloat() * 2 }))

        val array = list.toArray()
        for (i in 0 until 1001) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun ensureSizeHeapList() {
        val list = create(120)
        list.add(0.0f)
        list.ensureSize(1000)
        list.ensureSize(2000)
        list.add(HeapArrayFloatList().apply { add(FloatArray(1000) { 2.0f + it.toFloat() * 2 }) })

        val array = list.toArray()
        for (i in 0 until 1001) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun ensureSizeBufferedList() {
        val list = create(120)
        list.add(0.0f)
        list.ensureSize(1000)
        list.ensureSize(2000)
        list.add(BufferedArrayFloatList().apply { add(FloatArray(1000) { 2.0f + it.toFloat() * 2 }) })

        val array = list.toArray()
        for (i in 0 until 1001) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    @Test
    fun ensureSizeFragmentedList() {
        val list = create(120)
        list.add(0.0f)
        list.ensureSize(1000)
        list.ensureSize(2000)
        list.add(FragmentedArrayFloatList().apply { add(FloatArray(1000) { 2.0f + it.toFloat() * 2 }) })

        val array = list.toArray()
        for (i in 0 until 1001) {
            assertEquals(array[i], i.toFloat() * 2.0f)
        }
    }

    private fun wrap(vararg array: Float): FloatBuffer {
        val buffer = FloatBuffer.wrap(array)
        buffer.position(array.size)

        return buffer
    }
}
