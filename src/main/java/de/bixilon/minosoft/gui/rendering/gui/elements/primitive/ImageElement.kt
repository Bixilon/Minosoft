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

package de.bixilon.minosoft.gui.rendering.gui.elements.primitive

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.GuiDelegate
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

open class ImageElement(
    guiRenderer: GUIRenderer,
    texture: Texture?,
    uvStart: Vec2 = Vec2.EMPTY,
    uvEnd: Vec2 = Vec2(1.0f, 1.0f),
    size: Vec2 = texture?.size?.let { Vec2(it) } ?: Vec2.EMPTY,
    tint: RGBColor = ChatColors.WHITE,
) : Element(guiRenderer, GUIMesh.GUIMeshStruct.FLOATS_PER_VERTEX * 6) {

    var texture by GuiDelegate(texture)
    var uvStart by GuiDelegate(uvStart)
    var uvEnd by GuiDelegate(uvEnd)
    var tint by GuiDelegate(tint)

    init {
        this.preferredSize = size
        this.size = size
    }


    constructor(guiRenderer: GUIRenderer, texture: Texture, uvStart: Vec2i, uvEnd: Vec2i, size: Vec2 = Vec2(texture.size), tint: RGBColor = ChatColors.WHITE) : this(guiRenderer, texture, Vec2(uvStart) * texture.array.pixel, Vec2(uvEnd) * texture.array.pixel, size, tint)

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        consumer.addQuad(offset, offset + size, texture ?: return, uvStart, uvEnd, tint, options)
    }

    override fun update() {
        super.update()
        size = preferredSize?.min(maxSize) ?: Vec2.EMPTY
    }
}
