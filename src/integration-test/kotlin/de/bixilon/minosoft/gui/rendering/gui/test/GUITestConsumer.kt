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

package de.bixilon.minosoft.gui.rendering.gui.test

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMeshCache
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.ShaderIdentifiable
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh

class GUITestConsumer : GUIVertexConsumer {
    override val order get() = Mesh.QUAD_TO_QUAD_ORDER
    override fun ensureSize(size: Int) = Unit

    override fun addVertex(position: Vec2, texture: ShaderIdentifiable?, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?) {
        TODO("Not yet implemented")
    }

    override fun addCache(cache: GUIMeshCache) {
        TODO("Not yet implemented")
    }
}
