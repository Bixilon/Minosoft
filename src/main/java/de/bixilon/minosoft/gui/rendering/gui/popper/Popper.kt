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

package de.bixilon.minosoft.gui.rendering.gui.popper

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import glm_.vec2.Vec2i

abstract class Popper(
    guiRenderer: GUIRenderer,
    position: Vec2i,
) : Element(guiRenderer), LayoutedElement {
    private val background = ColorElement(guiRenderer, Vec2i.EMPTY, color = ChatColors.YELLOW)
    open var dead = false
    override var layoutOffset: Vec2i = position
        protected set
    var position = position
        set(value) {
            if (field == value) {
                return
            }
            field = value
            forceApply()
        }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        background.render(offset, consumer, options)
    }

    override fun forceSilentApply() {
        layoutOffset = position // ToDo
        background.size = size
        cacheUpToDate = false
    }

    fun show() {
        guiRenderer.popper += this
    }

    fun hide() {
        dead = true
    }
}
