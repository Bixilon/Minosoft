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

package de.bixilon.minosoft.gui.rendering.sky.box

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.MeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct

class SkyboxTextureMeshBuilder(context: RenderContext) : MeshBuilder(context, SkyboxTextureMeshStruct, PrimitiveTypes.TRIANGLE, initialCacheSize = 6 * 2 * 3 * SkyboxTextureMeshStruct.floats) {

    init {
        data += floatArrayOf(
            -1.0f, +1.0f, -1.0f, 0.buffer(),
            -1.0f, -1.0f, -1.0f, 3.buffer(),
            +1.0f, -1.0f, -1.0f, 1.buffer(),
            +1.0f, -1.0f, -1.0f, 1.buffer(),
            +1.0f, +1.0f, -1.0f, 2.buffer(),
            -1.0f, +1.0f, -1.0f, 0.buffer(),

            -1.0f, -1.0f, +1.0f, 1.buffer(),
            -1.0f, -1.0f, -1.0f, 3.buffer(),
            -1.0f, +1.0f, -1.0f, 0.buffer(),
            -1.0f, +1.0f, -1.0f, 0.buffer(),
            -1.0f, +1.0f, +1.0f, 2.buffer(),
            -1.0f, -1.0f, +1.0f, 1.buffer(),

            +1.0f, -1.0f, -1.0f, 3.buffer(),
            +1.0f, -1.0f, +1.0f, 1.buffer(),
            +1.0f, +1.0f, +1.0f, 2.buffer(),
            +1.0f, +1.0f, +1.0f, 2.buffer(),
            +1.0f, +1.0f, -1.0f, 0.buffer(),
            +1.0f, -1.0f, -1.0f, 3.buffer(),

            -1.0f, -1.0f, +1.0f, 3.buffer(),
            -1.0f, +1.0f, +1.0f, 1.buffer(),
            +1.0f, +1.0f, +1.0f, 2.buffer(),
            +1.0f, +1.0f, +1.0f, 2.buffer(),
            +1.0f, -1.0f, +1.0f, 0.buffer(),
            -1.0f, -1.0f, +1.0f, 3.buffer(),

            -1.0f, +1.0f, -1.0f, 3.buffer(),
            +1.0f, +1.0f, -1.0f, 0.buffer(),
            +1.0f, +1.0f, +1.0f, 2.buffer(),
            +1.0f, +1.0f, +1.0f, 2.buffer(),
            -1.0f, +1.0f, +1.0f, 1.buffer(),
            -1.0f, +1.0f, -1.0f, 3.buffer(),

            -1.0f, -1.0f, -1.0f, 3.buffer(),
            -1.0f, -1.0f, +1.0f, 1.buffer(),
            +1.0f, -1.0f, +1.0f, 2.buffer(),
            +1.0f, -1.0f, +1.0f, 2.buffer(),
            +1.0f, -1.0f, -1.0f, 0.buffer(),
            -1.0f, -1.0f, -1.0f, 3.buffer(),
        )
    }

    data class SkyboxTextureMeshStruct(
        val position: Vec3f,
        val uvIndex: Int,
    ) {
        companion object : MeshStruct(SkyboxTextureMeshStruct::class)
    }
}
