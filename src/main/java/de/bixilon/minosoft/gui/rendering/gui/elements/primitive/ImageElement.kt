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

package de.bixilon.minosoft.gui.rendering.gui.elements.primitive

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
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
    var texture: Texture? = texture
        set(value) {
            field = value
            cacheUpToDate = false
        }
    var uvStart: Vec2 = uvStart
        set(value) {
            field = value
            cacheUpToDate = false
        }
    var uvEnd: Vec2 = uvEnd
        set(value) {
            field = value
            cacheUpToDate = false
        }

    override var size: Vec2
        get() = super.size
        set(value) {
            super.size = value
            cacheUpToDate = false
        }

    override var prefSize: Vec2
        get() = size
        set(value) {
            size = value
        }

    var tint: RGBColor = tint
        set(value) {
            field = value
            cacheUpToDate = false
        }

    init {
        this.size = size
    }


    constructor(guiRenderer: GUIRenderer, texture: Texture, uvStart: Vec2i, uvEnd: Vec2i, size: Vec2 = Vec2(texture.size), tint: RGBColor = ChatColors.WHITE) : this(guiRenderer, texture, Vec2(uvStart) * texture.singlePixelSize, Vec2(uvEnd) * texture.singlePixelSize, size, tint)

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        consumer.addQuad(offset, offset + size, texture ?: return, uvStart, uvEnd, tint, options)
    }

    override fun forceSilentApply() = Unit
    override fun silentApply(): Boolean = false
}
