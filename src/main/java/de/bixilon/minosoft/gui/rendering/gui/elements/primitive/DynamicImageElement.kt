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

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureListener
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureState
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture

open class DynamicImageElement(
    guiRenderer: GUIRenderer,
    texture: DynamicTexture?,
    uvStart: Vec2f = Vec2f.EMPTY,
    uvEnd: Vec2f = Vec2f(1.0f, 1.0f),
    size: Vec2f = Vec2f.EMPTY,
    tint: RGBAColor = ChatColors.WHITE,
    parent: Element? = null,
) : Element(guiRenderer, GUIMesh.GUIMeshStruct.FLOATS_PER_VERTEX * 6), DynamicTextureListener {

    var texture: DynamicTexture? = null
        set(value) {
            field?.removeListener(this)
            value?.addListener(this)
            field = value
            cacheUpToDate = false
        }
    var uvStart: Vec2f = uvStart
        set(value) {
            field = value
            cacheUpToDate = false
        }
    var uvEnd: Vec2f = uvEnd
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

    init {
        this.size = size
        this.texture = texture
        this.parent = parent
    }

    private fun getAvailableTexture(): ShaderTexture {
        val texture = texture ?: return context.textures.whiteTexture.texture
        if (texture.state != DynamicTextureState.LOADED) {
            return context.textures.whiteTexture.texture
        }
        return texture
    }

    override fun forceRender(offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        consumer.addQuad(offset, offset + size, getAvailableTexture(), uvStart, uvEnd, tint, options)
    }

    override fun forceSilentApply() = Unit
    override fun silentApply(): Boolean = false

    override fun onDynamicTextureChange(texture: DynamicTexture): Boolean {
        if (texture === this.texture) {
            forceApply()
            return false
        }
        return true
    }
}
