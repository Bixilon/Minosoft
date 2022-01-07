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

package de.bixilon.minosoft.data.world.container.palette

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.world.container.palette.data.PaletteData
import de.bixilon.minosoft.data.world.container.palette.palettes.Palette

class PalettedContainer<T>(
    private val edgeBits: Int,
    val palette: Palette<T>,
    val data: PaletteData,
) {

    fun get(x: Int, y: Int, z: Int): T {
        return palette.get(data.get((((y shl edgeBits) or z) shl edgeBits) or x))
    }

    inline fun <reified V : T> unpack(): Array<V> {
        val array: Array<V?> = arrayOfNulls(data.size)
        for (i in array.indices) {
            array[i] = palette.get(data.get(i)) as V
        }
        return array.unsafeCast()
    }
}
