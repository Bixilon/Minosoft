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

package de.bixilon.minosoft.gui.rendering.util.vec.vec3

import glm_.vec3.Vec3d

object Vec3dUtil {

    val Vec3d.Companion.MIN: Vec3d
        get() = Vec3d(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE)

    val Vec3d.Companion.EMPTY: Vec3d
        get() = Vec3d(0.0, 0.0, 0.0)

    val Vec3d.Companion.ONE: Vec3d
        get() = Vec3d(1.0, 1.0, 1.0)

    val Vec3d.Companion.MAX: Vec3d
        get() = Vec3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)

}
