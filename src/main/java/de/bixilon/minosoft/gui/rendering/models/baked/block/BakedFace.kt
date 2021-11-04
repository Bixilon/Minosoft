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

package de.bixilon.minosoft.gui.rendering.models.baked.block

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMesh
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class BakedFace(
    val positions: Array<Vec3>,
    val uv: Array<Vec2>,
    val shade: Boolean,
    val tintIndex: Int,
    val cullFace: Directions?,
    val texture: AbstractTexture,
) {
    fun singleRender(position: Vec3i, mesh: ChunkSectionMesh, light: Int, ambientLight: IntArray) {
        val floatPosition = Vec3(position)

        for (index in DRAW_ORDER) {
            mesh.addVertex(positions[index] + floatPosition, uv[index], texture, null, light)
        }
    }


    companion object {
        private val DRAW_ORDER = intArrayOf(
            0,
            1,
            3,
            3,
            2,
            0,
        )
    }
}
