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

package de.bixilon.minosoft.data.direction

import de.bixilon.kutil.array.ArrayUtil
import de.bixilon.minosoft.data.Axes

object DirectionUtil {

    fun Directions.rotateX(count: Int = 1): Directions {
        if (count == 0) return this
        if (axis == Axes.X) return this
        var count = count % Directions.SIZE_SIDES
        if (count < 0) count += Directions.SIZE_SIDES

        return Directions.INDEXED[0][ArrayUtil.modifyArrayIndex(index.x + count, Directions.SIZE_SIDES)]
    }

    fun Directions.rotateY(count: Int = 1): Directions {
        if (count == 0) return this
        if (axis == Axes.Y) return this
        var count = count % Directions.SIZE_SIDES
        if (count < 0) count += Directions.SIZE_SIDES

        return Directions.INDEXED[1][ArrayUtil.modifyArrayIndex(index.y + count, Directions.SIZE_SIDES)]
    }
}
