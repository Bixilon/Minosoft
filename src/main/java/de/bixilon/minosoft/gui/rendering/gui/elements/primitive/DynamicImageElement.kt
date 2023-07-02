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
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicStateChangeCallback
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureState
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderIdentifiable
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

open class DynamicImageElement(
    guiRenderer: GUIRenderer,
    texture: DynamicTexture?,
    uvStart: Vec2 = Vec2.EMPTY,
    uvEnd: Vec2 = Vec2(1.0f, 1.0f),
    size: Vec2 = Vec2.EMPTY,
    tint: RGBColor = ChatColors.WHITE,
    parent: Element? = null,
) : Element(guiRenderer, GUIMesh.GUIMeshStruct.FLOATS_PER_VERTEX * 6), DynamicStateChangeCallback {

    var texture: DynamicTexture? = null
        set(value) {
            field?.usages?.decrementAndGet()
            field?.removeListener(this)
            value?.usages?.incrementAndGet()
            value?.addListener(this)
            field = value
            cache.invalidate()
        }
    var uvStart: Vec2 = uvStart
        set(value) {
            field = value
            cache.invalidate()
        }
    var uvEnd: Vec2 = uvEnd
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

    init {
        this.size = size
        this.texture = texture
        this.parent = parent
    }

    private fun getAvailableTexture(): ShaderIdentifiable {
        val texture = texture ?: return context.textures.whiteTexture.texture
        if (texture.state != DynamicTextureState.LOADED) {
            return context.textures.whiteTexture.texture
        }
        return texture
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        consumer.addQuad(offset, offset + size, getAvailableTexture(), uvStart, uvEnd, tint, options)
    }

    override fun forceSilentApply() = Unit
    override fun silentApply(): Boolean = false

    protected fun finalize() {
        texture?.usages?.decrementAndGet()
    }

    override fun onStateChange(texture: DynamicTexture, state: DynamicTextureState) {
        if (texture === this.texture) {
            forceApply()
        }
    }
}
