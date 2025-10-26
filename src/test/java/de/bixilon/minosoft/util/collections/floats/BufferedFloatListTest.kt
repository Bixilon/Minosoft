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

import de.bixilon.kutil.benchmark.BenchmarkUtil
import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import org.junit.jupiter.api.Test

class BufferedFloatListTest : DirectFloatListTest() {

    override fun create(initialSize: Int): AbstractFloatList {
        return BufferedArrayFloatList(initialSize)
    }


    // @Test
    fun benchmark() {
        BenchmarkUtil.benchmark(1000) {
            val list = BufferedArrayFloatList(1024)

            for (i in 0 until 100000) {
                list.add(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f)
            }
            list.unload()
        }.println()
    }
}
