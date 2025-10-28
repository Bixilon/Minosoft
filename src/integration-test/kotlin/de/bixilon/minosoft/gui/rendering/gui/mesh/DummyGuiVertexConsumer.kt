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
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.array.PackedUVArray

open class DummyGuiVertexConsumer : GuiVertexConsumer {
    var char = 0

    override fun ensureSize(primitives: Int) = Broken()

    open fun addChar(start: Vec2f, end: Vec2f, index: Int): Unit = Broken()
    override fun addQuad(positions: FloatArray, uv: PackedUVArray, texture: ShaderTexture, tint: RGBAColor, options: GUIVertexOptions?) = Broken()


    override fun addChar(start: Vec2f, end: Vec2f, texture: ShaderTexture, uvStart: Vec2f, uvEnd: Vec2f, italic: Boolean, tint: RGBAColor, options: GUIVertexOptions?) {
        addChar(Vec2f(start.unsafe), Vec2f(end.unsafe), this.char++)
    }

    override fun addQuad(startX: Float, startY: Float, endX: Float, endY: Float, texture: ShaderTexture, uvStartX: Float, uvStartY: Float, uvEndX: Float, uvEndY: Float, tint: RGBAColor, options: GUIVertexOptions?) {}

    override fun addQuad(start: Vec2f, end: Vec2f, tint: RGBAColor, options: GUIVertexOptions?) {}

}
