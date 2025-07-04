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

package de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.sort

import glm_.vec3.Vec3d
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil

enum class Sorting {
    NEAREST,
    FURTHEST,
    RANDOM,
    ARBITRARY,
    ;


    fun sort(center: Vec3d, selected: MutableList<Entity>) {
        when (this) {
            NEAREST -> selected.sortBy { Vec3dUtil.distance2(center, it.physics.position) }
            FURTHEST -> selected.sortBy { -Vec3dUtil.distance2(center, it.physics.position) }
            RANDOM -> selected.shuffle()
            ARBITRARY -> selected.sortBy { it.id ?: 0 }
        }
    }

    companion object : ValuesEnum<Sorting> {
        override val VALUES: Array<Sorting> = values()
        override val NAME_MAP: Map<String, Sorting> = EnumUtil.getEnumValues(VALUES)
    }
}
