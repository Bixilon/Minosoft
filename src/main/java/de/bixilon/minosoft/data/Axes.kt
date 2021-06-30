/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.properties.serializer.BlockPropertiesSerializer
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import glm_.vec3.Vec3t

enum class Axes {
    X,
    Y,
    Z,
    ;

    fun <T : Number> choose(vec3: Vec3t<T>): T {
        return when (this) {
            X -> vec3.x
            Y -> vec3.y
            Z -> vec3.z
        }
    }

    companion object : ValuesEnum<Axes>, BlockPropertiesSerializer {
        override val VALUES: Array<Axes> = values()
        override val NAME_MAP: Map<String, Axes> = KUtil.getEnumValues(VALUES)

        operator fun get(direction: Directions): Axes {
            return when (direction) {
                Directions.EAST, Directions.WEST -> X
                Directions.UP, Directions.DOWN -> Y
                Directions.NORTH, Directions.SOUTH -> Z
            }
        }

        override fun deserialize(value: Any): Axes {
            return NAME_MAP[value] ?: throw IllegalArgumentException("No such property: $value")
        }
    }
}
