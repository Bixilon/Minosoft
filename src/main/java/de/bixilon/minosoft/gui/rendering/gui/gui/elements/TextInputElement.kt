/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.gui.elements

import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import glm_.vec2.Vec2i

class TextInputElement(
    guiRenderer: GUIRenderer,
    val maxLength: Int = Int.MAX_VALUE,
) : Element(guiRenderer) {
    private val textElement = TextElement(guiRenderer, "", background = false, parent = this)
    private val background = ColorElement(guiRenderer, Vec2i.EMPTY, RenderConstants.TEXT_BACKGROUND_COLOR)
    var value: String = ""
        set(value) {
            if (field == value) {
                return
            }
            field = value
            forceSilentApply()
        }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        background.render(offset, z, consumer, options)
        return textElement.render(offset, z, consumer, options)
    }

    override fun forceSilentApply() {
        textElement.text = value
        textElement.silentApply()
        background.size = Vec2i(prefMaxSize.x, prefMaxSize.y)
        cacheUpToDate = false
    }

    override fun onCharPress(char: Int) {
        if (value.length >= maxLength) {
            return
        }
        value += char.toChar()
        forceSilentApply()
    }
}
