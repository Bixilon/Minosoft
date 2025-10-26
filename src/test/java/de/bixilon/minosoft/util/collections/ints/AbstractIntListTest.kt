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

package de.bixilon.minosoft.util.collections.ints

import de.bixilon.kutil.collections.primitive.ints.HeapIntList
import de.bixilon.kutil.collections.primitive.ints.IntList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.IntBuffer
import kotlin.test.assertContentEquals

abstract class AbstractIntListTest {

    abstract fun create(initialSize: Int = 1): IntList

    @Test
    fun initialListSize() {
        val list = create()
        assertEquals(0, list.size)
    }

    @Test
    fun addSingleFloat() {
        val list = create()
        list.add(189)
        assertEquals(1, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189), array)
    }

    @Test
    fun addMultipleFloats() {
        val list = create()
        list.add(189)
        list.add(289)
        list.add(389)
        assertEquals(3, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189, 289, 389), array)
    }

    @Test
    fun addVastFloats() {
        val list = create()
        for (i in 0 until 1000) {
            list.add(i * 2)
        }
        assertEquals(1000, list.size)
        val array = list.toArray()
        for (i in 0 until 1000) {
            assertEquals(array[i], i * 2)
        }
    }

    @Test
    fun addSingleArray() {
        val list = create()
        list.add(intArrayOf(189))
        assertEquals(1, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189), array)
    }

    @Test
    fun addMultipleArrays() {
        val list = create()
        list.add(intArrayOf(189))
        list.add(intArrayOf(289))
        list.add(intArrayOf(389))
        assertEquals(3, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189, 289, 389), array)
    }

    @Test
    fun addMultipleBigArrays() {
        val list = create()
        list.add(intArrayOf(189))
        list.add(intArrayOf(289, 389))
        list.add(intArrayOf(489, 589, 689))
        assertEquals(6, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189, 289, 389, 489, 589, 689), array)
    }

    @Test
    fun addVastArrays() {
        val list = create()

        for (i in 0 until 1000) {
            list.add(intArrayOf(i * 2))
        }
        assertEquals(1000, list.size)
        val array = list.toArray()
        for (i in 0 until 1000) {
            assertEquals(array[i], i * 2)
        }
    }

    @Test
    fun addSingleBuffer() {
        val list = create()
        list.add(wrap(189))
        assertEquals(1, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189), array)
    }

    @Test
    fun addMultipleBuffers() {
        val list = create()
        list.add(wrap(189))
        list.add(wrap(289))
        list.add(wrap(389))
        assertEquals(3, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189, 289, 389), array)
    }

    @Test
    fun addMultipleBigBuffers() {
        val list = create()
        list.add(wrap(189))
        list.add(wrap(289, 389))
        list.add(wrap(489, 589, 689))
        assertEquals(6, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189, 289, 389, 489, 589, 689), array)
    }

    @Test
    fun addVastBuffers() {
        val list = create()

        for (i in 0 until 1000) {
            list.add(wrap(i * 2))
        }
        assertEquals(1000, list.size)
        val array = list.toArray()
        for (i in 0 until 1000) {
            assertEquals(array[i], i * 2)
        }
    }


    @Test
    fun addSingleHeapList() {
        val list = create()
        list.add(HeapIntList(1).apply { add(189) })
        assertEquals(1, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189), array)
    }

    @Test
    fun addMultipleHeapLists() {
        val list = create()
        list.add(HeapIntList(1).apply { add(189) })
        list.add(HeapIntList(1).apply { add(289) })
        list.add(HeapIntList(1).apply { add(389) })
        assertEquals(3, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189, 289, 389), array)
    }

    @Test
    fun addMultipleBigHeapLists() {
        val list = create()
        list.add(HeapIntList(1).apply { add(189) })
        list.add(HeapIntList(1).apply { add(289); add(389) })
        list.add(HeapIntList(1).apply { add(489); add(589); add(689) })

        assertEquals(6, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189, 289, 389, 489, 589, 689), array)
    }

    @Test
    fun addVastHeapLists() {
        val list = create()

        for (i in 0 until 1000) {
            list.add(HeapIntList(1).apply { add(i * 2) })
        }
        assertEquals(1000, list.size)
        val array = list.toArray()
        for (i in 0 until 1000) {
            assertEquals(array[i], i * 2)
        }
    }


    @Test
    fun addSingleBufferedList() {
        val list = create()
        list.add(BufferIntList(1).apply { add(189) })
        assertEquals(1, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189), array)
    }

    @Test
    fun addMultipleBufferedLists() {
        val list = create()
        list.add(BufferIntList(1).apply { add(189) })
        list.add(BufferIntList(1).apply { add(289) })
        list.add(BufferIntList(1).apply { add(389) })
        assertEquals(3, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189, 289, 389), array)
    }

    @Test
    fun addMultipleBigBufferedLists() {
        val list = create()
        list.add(BufferIntList(1).apply { add(189) })
        list.add(BufferIntList(1).apply { add(289); add(389) })
        list.add(BufferIntList(1).apply { add(489); add(589); add(689) })

        assertEquals(6, list.size)
        val array = list.toArray()
        assertContentEquals(intArrayOf(189, 289, 389, 489, 589, 689), array)
    }

    @Test
    fun addVastBufferedLists() {
        val list = create()

        for (i in 0 until 1000) {
            list.add(BufferIntList(1).apply { add(i * 2) })
        }
        assertEquals(1000, list.size)
        val array = list.toArray()
        for (i in 0 until 1000) {
            assertEquals(array[i], i * 2)
        }
    }

    @Test
    fun fragmentedArray() {
        val list = create(120)
        list.add(IntArray(110) { it * 2 })
        list.add(IntArray(50) { 220 + it * 2 })
        list.add(320)

        val array = list.toArray()
        for (i in 0 until 161) {
            assertEquals(array[i], i * 2)
        }
    }

    @Test
    fun fragmentedBuffer() {
        val list = create(120)
        list.add(wrap(*IntArray(110) { it * 2 }))
        list.add(wrap(*IntArray(50) { 220 + it * 2 }))
        list.add(320)

        val array = list.toArray()
        for (i in 0 until 161) {
            assertEquals(array[i], i * 2)
        }
    }

    @Test
    fun fragmentedHeapList() {
        val list = create(120)
        list.add(HeapIntList().apply { add(IntArray(110) { it * 2 }) })
        list.add(HeapIntList().apply { add(IntArray(50) { 220 + it * 2 }) })
        list.add(320)

        val array = list.toArray()
        for (i in 0 until 161) {
            assertEquals(array[i], i * 2)
        }
    }

    @Test
    fun fragmentedBufferedList() {
        val list = create(120)
        list.add(BufferIntList().apply { add(IntArray(110) { it * 2 }) })
        list.add(BufferIntList().apply { add(IntArray(50) { 220 + it * 2 }) })
        list.add(320)

        val array = list.toArray()
        for (i in 0 until 161) {
            assertEquals(array[i], i * 2)
        }
    }

    @Test
    fun ensureSizeArray() {
        val list = create(120)
        list.add(0)
        list.ensureSize(1000)
        list.ensureSize(2000)
        list.add(IntArray(1000) { 2 + it * 2 })

        val array = list.toArray()
        for (i in 0 until 1001) {
            assertEquals(array[i], i * 2)
        }
    }

    @Test
    fun ensureSizeBuffer() {
        val list = create(120)
        list.add(0)
        list.ensureSize(1000)
        list.ensureSize(2000)
        list.add(wrap(*IntArray(1000) { 2 + it * 2 }))

        val array = list.toArray()
        for (i in 0 until 1001) {
            assertEquals(array[i], i * 2)
        }
    }

    @Test
    fun ensureSizeHeapList() {
        val list = create(120)
        list.add(0)
        list.ensureSize(1000)
        list.ensureSize(2000)
        list.add(HeapIntList().apply { add(IntArray(1000) { 2 + it * 2 }) })

        val array = list.toArray()
        for (i in 0 until 1001) {
            assertEquals(array[i], i * 2)
        }
    }

    @Test
    fun ensureSizeBufferedList() {
        val list = create(120)
        list.add(0)
        list.ensureSize(1000)
        list.ensureSize(2000)
        list.add(BufferIntList().apply { add(IntArray(1000) { 2 + it * 2 }) })

        val array = list.toArray()
        for (i in 0 until 1001) {
            assertEquals(array[i], i * 2)
        }
    }


    @Test
    fun clear() {
        val list = create()
        list.add(1)
        list.add(2)
        list.clear()
        assertEquals(0, list.toArray().size)
        list.add(3)
        list.add(4)
        assertContentEquals(intArrayOf(3, 4), list.toArray())
    }

    @Test
    fun clearBig() {
        val list = create()
        list.add(1)
        list.add(IntArray(1000) { 1 + it })
        list.add(IntArray(2000) { 1001 + it })
        list.clear()
        assertEquals(0, list.toArray().size)
        list.add(3)
        list.add(4)
        assertContentEquals(intArrayOf(3, 4), list.toArray())
    }

    private fun wrap(vararg array: Int): IntBuffer {
        val buffer = IntBuffer.wrap(array)

        return buffer
    }
}
