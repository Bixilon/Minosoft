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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked

import de.bixilon.minosoft.data.direction.Directions

enum class Shades(val shade: Float) {
    DOWN(0.5f),
    UP(1.0f),
    X(0.6f),
    Z(0.8f),
    ;

    val color = color()


    private fun color(): Int {
        val int = (shade * 255).toInt()

        return (int shl 16) or (int shl 8) or int
    }

    companion object {


        val Directions.shade: Shades
            get() = when (this) {
                Directions.DOWN -> DOWN
                Directions.UP -> UP
                Directions.NORTH, Directions.SOUTH -> Z
                Directions.WEST, Directions.EAST -> X
            }
    }
}
