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

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kutil.collections.primitive.floats.FloatList
import de.bixilon.kutil.collections.primitive.floats.HeapFloatList
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.CachedGuiVertexConsumer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes

class GuiMeshCache(
    val context: RenderContext,
    override var halfSize: Vec2f,
    estimate: Int = 12,
    override var data: FloatList = HeapFloatList(estimate * PrimitiveTypes.QUAD.vertices * GuiMeshBuilder.GUIMeshStruct.floats),
) : CachedGuiVertexConsumer {
    override val white = context.textures.whiteTexture.texture

    var revision = 0L
    var offset: Vec2f = Vec2f.EMPTY
    var options: GUIVertexOptions? = null

    fun clear() {
        data.clear()
        revision++
    }

    override fun ensureSize(primitives: Int) {
        data.ensureSize(primitives * PrimitiveTypes.QUAD.vertices * GuiMeshBuilder.GUIMeshStruct.floats)
    }
}
