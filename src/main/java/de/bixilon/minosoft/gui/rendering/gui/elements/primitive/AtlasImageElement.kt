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

package de.bixilon.minosoft.gui.rendering.gui.elements.primitive

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMeshBuilder
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.TexturePart
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

open class AtlasImageElement(
    guiRenderer: GUIRenderer,
    texturePart: TexturePart?,
    size: Vec2f = texturePart?.size?.let { Vec2f(it) } ?: Vec2f.EMPTY,
    tint: RGBAColor = ChatColors.WHITE,
) : Element(guiRenderer, GUIMeshBuilder.GUIMeshStruct.floats * 6) {
    var texture: Texture? = texturePart?.texture
        set(value) {
            field = value
            cacheUpToDate = false
        }
    var uvStart: Vec2f? = null
        set(value) {
            field = value
            cacheUpToDate = false
        }
    var uvEnd: Vec2f? = null
        set(value) {
            field = value
            cacheUpToDate = false
        }

    override var size: Vec2f
        get() = super.size
        set(value) {
            super.size = value
            cacheUpToDate = false
        }

    override var prefSize: Vec2f
        get() = size
        set(value) {
            size = value
        }

    var tint: RGBAColor = tint
        set(value) {
            field = value
            cacheUpToDate = false
        }

    var texturePart: TexturePart? = texturePart
        set(value) {
            if (field === value) {
                return
            }
            texture = value?.texture
            field = value
            uvStart = null
            uvEnd = null

            cacheUpToDate = false
        }

    init {
        this.size = size
    }


    constructor(
        guiRenderer: GUIRenderer,
        texturePart: TexturePart?,
        size: Vec2i,
        tint: RGBAColor = ChatColors.WHITE,
    ) : this(guiRenderer, texturePart, Vec2f(size), tint)


    override fun forceRender(offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val texture = texture ?: return
        val textureLike = texturePart ?: return
        consumer.addQuad(offset, offset + size, texture, uvStart ?: textureLike.uvStart, uvEnd ?: textureLike.uvEnd, tint, options)
    }

    override fun forceSilentApply() = Unit
    override fun silentApply(): Boolean = false
}
