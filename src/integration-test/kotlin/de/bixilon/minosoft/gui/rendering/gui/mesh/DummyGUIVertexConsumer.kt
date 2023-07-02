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

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderIdentifiable
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh

open class DummyGUIVertexConsumer : GUIVertexConsumer {
    override val order: Array<Pair<Int, Int>> get() = Mesh.QUAD_TO_QUAD_ORDER
    var caches = 0
    var char = 0

    override fun addCache(cache: GUIMeshCache) {
        caches++
    }

    override fun ensureSize(size: Int) {
        TODO("Not yet implemented")
    }

    override fun addVertex(position: Vec2, texture: ShaderIdentifiable?, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?) {
        TODO("Not yet implemented")
    }

    override fun addChar(start: Vec2, end: Vec2, texture: Texture?, uvStart: Vec2, uvEnd: Vec2, italic: Boolean, tint: RGBColor, options: GUIVertexOptions?) {
        addChar(start, end, this.char++)
    }

    open fun addChar(start: Vec2, end: Vec2, index: Int) {
        TODO("Abstract")
    }
}
