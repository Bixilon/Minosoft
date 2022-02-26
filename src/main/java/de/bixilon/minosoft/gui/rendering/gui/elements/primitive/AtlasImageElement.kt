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

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.TextureLike
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

open class AtlasImageElement(
    guiRenderer: GUIRenderer,
    textureLike: TextureLike?,
    size: Vec2i = textureLike?.size ?: Vec2i.EMPTY,
    tint: RGBColor = ChatColors.WHITE,
) : Element(guiRenderer, GUIMesh.GUIMeshStruct.FLOATS_PER_VERTEX * 6) {
    var texture: AbstractTexture? = textureLike?.texture
        set(value) {
            field = value
            cacheUpToDate = false
        }
    var uvStart: Vec2? = null
        set(value) {
            field = value
            cacheUpToDate = false
        }
    var uvEnd: Vec2? = null
        set(value) {
            field = value
            cacheUpToDate = false
        }

    override var size: Vec2i
        get() = super.size
        set(value) {
            super.size = value
            cacheUpToDate = false
        }

    override var prefSize: Vec2i
        get() = size
        set(value) {
            size = value
        }

    var tint: RGBColor = tint
        set(value) {
            field = value
            cacheUpToDate = false
        }

    var textureLike: TextureLike? = textureLike
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


    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val texture = texture ?: return
        val textureLike = textureLike ?: return
        consumer.addQuad(offset, offset + size, texture, uvStart ?: textureLike.uvStart, uvEnd ?: textureLike.uvEnd, tint, options)
    }

    override fun forceSilentApply() = Unit
    override fun silentApply(): Boolean = false
}
