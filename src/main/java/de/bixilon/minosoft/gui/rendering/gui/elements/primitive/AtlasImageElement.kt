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
import de.bixilon.minosoft.gui.rendering.gui.atlas.TexturePart
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

open class AtlasImageElement(
    guiRenderer: GUIRenderer,
    texturePart: TexturePart?,
    size: Vec2 = texturePart?.size?.let { Vec2(it) } ?: Vec2.EMPTY,
    tint: RGBColor = ChatColors.WHITE,
) : Element(guiRenderer, GUIMesh.GUIMeshStruct.FLOATS_PER_VERTEX * 6) {
    var texture: Texture? = texturePart?.texture
        set(value) {
            field = value
            cache.invalidate()
        }
    var uvStart: Vec2? = null
        set(value) {
            field = value
            cache.invalidate()
        }
    var uvEnd: Vec2? = null
        set(value) {
            field = value
            cache.invalidate()
        }

    override var size: Vec2
        get() = super.size
        set(value) {
            super.size = value
            cache.invalidate()
        }

    override var prefSize: Vec2
        get() = size
        set(value) {
            size = value
        }

    var tint: RGBColor = tint
        set(value) {
            field = value
            cache.invalidate()
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

            cache.invalidate()
        }

    init {
        this.size = size
    }


    constructor(
        guiRenderer: GUIRenderer,
        texturePart: TexturePart?,
        size: Vec2i,
        tint: RGBColor = ChatColors.WHITE,
    ) : this(guiRenderer, texturePart, Vec2(size), tint)


    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val texture = texture ?: return
        val textureLike = texturePart ?: return
        consumer.addQuad(offset, offset + size, texture, uvStart ?: textureLike.uvStart, uvEnd ?: textureLike.uvEnd, tint, options)
    }

    override fun forceSilentApply() = Unit
    override fun silentApply(): Boolean = false
}
