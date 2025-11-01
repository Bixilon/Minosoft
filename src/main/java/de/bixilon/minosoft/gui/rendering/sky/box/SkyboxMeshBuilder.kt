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

import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadMeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.integrated.PositionOnlyMeshStruct

class SkyboxMeshBuilder(context: RenderContext) : QuadMeshBuilder(context, PositionOnlyMeshStruct, 6) {

    private inline fun addVertex(x: Float, y: Float, z: Float) = data.add(
        x, y, z,
    )

    init {
        addVertex(-1.0f, -1.0f, -1.0f)
        addVertex(-1.0f, -1.0f, +1.0f)
        addVertex(+1.0f, -1.0f, +1.0f)
        addVertex(+1.0f, -1.0f, -1.0f)
        addIndexQuad(false, true)

        addVertex(-1.0f, +1.0f, -1.0f)
        addVertex(+1.0f, +1.0f, -1.0f)
        addVertex(+1.0f, +1.0f, +1.0f)
        addVertex(-1.0f, +1.0f, +1.0f)
        addIndexQuad(false, true)


        addVertex(-1.0f, -1.0f, -1.0f)
        addVertex(+1.0f, -1.0f, -1.0f)
        addVertex(+1.0f, +1.0f, -1.0f)
        addVertex(-1.0f, +1.0f, -1.0f)
        addIndexQuad(false, true)

        addVertex(-1.0f, -1.0f, +1.0f)
        addVertex(-1.0f, +1.0f, +1.0f)
        addVertex(+1.0f, +1.0f, +1.0f)
        addVertex(+1.0f, -1.0f, +1.0f)
        addIndexQuad(false, true)


        addVertex(-1.0f, -1.0f, -1.0f)
        addVertex(-1.0f, +1.0f, -1.0f)
        addVertex(-1.0f, +1.0f, +1.0f)
        addVertex(-1.0f, -1.0f, +1.0f)
        addIndexQuad(false, true)

        addVertex(+1.0f, -1.0f, -1.0f)
        addVertex(+1.0f, -1.0f, +1.0f)
        addVertex(+1.0f, +1.0f, +1.0f)
        addVertex(+1.0f, +1.0f, -1.0f)
        addIndexQuad(false, true)
    }
}
