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

package de.bixilon.minosoft.gui.rendering.gui.elements.button

import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isGreater
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isSmaller
import glm_.vec2.Vec2i

class ButtonElement(
    guiRenderer: GUIRenderer,
    text: Any,
    var onSubmit: () -> Unit,
) : Element(guiRenderer) {
    private val textElement = TextElement(guiRenderer, text, background = false).apply { parent = this@ButtonElement }
    private val disabledAtlas = guiRenderer.atlasManager["button_disabled"]
    private val normalAtlas = guiRenderer.atlasManager["button_normal"]
    private val hoveredButton = guiRenderer.atlasManager["button_hovered"]

    private val background = AtlasImageElement(guiRenderer, normalAtlas ?: guiRenderer.renderWindow.WHITE_TEXTURE).apply { parent = this@ButtonElement }

    var state: ButtonStates = ButtonStates.NORMAL
        set(value) {
            if (field == value) {
                return
            }
            field = value
            background.textureLike = when (value) {
                ButtonStates.NORMAL -> normalAtlas
                ButtonStates.HOVERED -> hoveredButton
            } ?: renderWindow.WHITE_TEXTURE
            forceApply()
        }

    init {
        size = textElement.size + Vec2i(4, 4)
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        var zUsed = background.render(offset, z, consumer, options)
        zUsed += textElement.render(offset + Vec2i(2, 2), z + zUsed, consumer, options)
        return zUsed
    }

    override fun forceSilentApply() {
        cacheUpToDate = false
        background.size = size
    }

    override fun onKeyPress(type: KeyChangeTypes, key: KeyCodes) {
        if (type == KeyChangeTypes.PRESS && key == KeyCodes.KEY_LEFT) {
            submit()
        }
    }

    override fun onMouseMove(position: Vec2i) {
        state = if (position isGreater size || position isSmaller Vec2i.EMPTY) {
            // move away
            ButtonStates.NORMAL
        } else {
            ButtonStates.HOVERED
        }
    }

    fun submit() {
        onSubmit()
    }
}
