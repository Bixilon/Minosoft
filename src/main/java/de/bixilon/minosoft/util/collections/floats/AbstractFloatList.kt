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

import de.bixilon.minosoft.util.collections.AbstractPrimitiveList
import java.nio.FloatBuffer

@Deprecated("Kutil")
abstract class AbstractFloatList : AbstractPrimitiveList<Float>() {

    abstract fun add(array: FloatArray)
    operator fun plusAssign(array: FloatArray) = add(array)
    abstract fun add(floatList: AbstractFloatList)
    operator fun plusAssign(floatList: AbstractFloatList) = add(floatList)
    abstract fun add(buffer: FloatBuffer)
    operator fun plusAssign(buffer: FloatBuffer) = add(buffer)

    abstract fun toArray(): FloatArray
}
