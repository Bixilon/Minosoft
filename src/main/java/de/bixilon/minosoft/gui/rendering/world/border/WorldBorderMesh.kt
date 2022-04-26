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

package de.bixilon.minosoft.gui.rendering.world.border

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct

class WorldBorderMesh(renderWindow: RenderWindow) : Mesh(renderWindow, WorldBorderMeshStruct, PrimitiveTypes.TRIANGLE, initialCacheSize = 6 * 2 * 3 * WorldBorderMeshStruct.FLOATS_PER_VERTEX) {

    init {
        data.addAll(floatArrayOf(
            -1.0f, +1.0f, -1.0f, Float.fromBits(1),
            +1.0f, -1.0f, -1.0f, Float.fromBits(0),
            -1.0f, -1.0f, -1.0f, Float.fromBits(3),
            +1.0f, -1.0f, -1.0f, Float.fromBits(0),
            -1.0f, +1.0f, -1.0f, Float.fromBits(1),
            +1.0f, +1.0f, -1.0f, Float.fromBits(2),

            -1.0f, -1.0f, +1.0f, Float.fromBits(3),
            -1.0f, +1.0f, -1.0f, Float.fromBits(2),
            -1.0f, -1.0f, -1.0f, Float.fromBits(0),
            -1.0f, +1.0f, -1.0f, Float.fromBits(2),
            -1.0f, -1.0f, +1.0f, Float.fromBits(3),
            -1.0f, +1.0f, +1.0f, Float.fromBits(1),

            +1.0f, -1.0f, -1.0f, Float.fromBits(3),
            +1.0f, +1.0f, +1.0f, Float.fromBits(2),
            +1.0f, -1.0f, +1.0f, Float.fromBits(0),
            +1.0f, +1.0f, +1.0f, Float.fromBits(2),
            +1.0f, -1.0f, -1.0f, Float.fromBits(3),
            +1.0f, +1.0f, -1.0f, Float.fromBits(1),

            -1.0f, -1.0f, +1.0f, Float.fromBits(0),
            +1.0f, +1.0f, +1.0f, Float.fromBits(1),
            -1.0f, +1.0f, +1.0f, Float.fromBits(2),
            +1.0f, +1.0f, +1.0f, Float.fromBits(1),
            -1.0f, -1.0f, +1.0f, Float.fromBits(0),
            +1.0f, -1.0f, +1.0f, Float.fromBits(3),
        ))
    }

    data class WorldBorderMeshStruct(
        val position: Vec3,
        val uvIndex: Int,
    ) {
        companion object : MeshStruct(WorldBorderMeshStruct::class)
    }
}
