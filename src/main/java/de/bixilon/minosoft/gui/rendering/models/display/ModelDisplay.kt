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

package de.bixilon.minosoft.gui.rendering.models.display

import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.toVec3i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

data class ModelDisplay(
    val rotation: Vec3i?,
    val translation: Vec3i?,
    val scale: Vec3?,
) {
    companion object {
        operator fun invoke(data: Map<String, Any>): ModelDisplay {
            return ModelDisplay(
                rotation = data["rotation"]?.toVec3i(),
                translation = data["translation"]?.toVec3i(),
                scale = data["scale"]?.toVec3(),
            )
        }
    }
}
