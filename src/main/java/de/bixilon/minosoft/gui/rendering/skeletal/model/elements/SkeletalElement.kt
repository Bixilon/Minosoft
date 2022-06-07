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

package de.bixilon.minosoft.gui.rendering.skeletal.model.elements

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.faces.SkeletalFace
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.ONE
import java.util.*

data class SkeletalElement(
    val name: String,
    val rescale: Boolean = false,
    val visible: Boolean = true,
    val locked: Boolean = false,
    val from: Vec3 = Vec3.EMPTY,
    val to: Vec3 = Vec3.ONE,
    val autouv: Int = 0,
    val color: Int = 0,
    val rotation: Vec3 = Vec3.EMPTY,
    val origin: Vec3 = Vec3.EMPTY,
    val uvOffset: Vec2 = Vec2.EMPTY,
    val faces: Map<Directions, SkeletalFace> = emptyMap(),
    val uuid: UUID,
)
