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

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isSmaller
import glm_.vec2.Vec2i

abstract class Popper(
    guiRenderer: GUIRenderer,
    position: Vec2i,
) : Element(guiRenderer), LayoutedElement {
    private val background = ColorElement(guiRenderer, Vec2i.EMPTY, color = RGBColor(10, 10, 20, 230))
    open var dead = false
    override var layoutOffset: Vec2i = EMPTY
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
        calculateLayoutOffset()
        background.size = size
        cacheUpToDate = false
    }


    private fun calculateLayoutOffset() {
        val windowSize = guiRenderer.scaledSize
        val position = position
        val size = size

        // must not be at the position
        // always try to make it on top of the position
        // ToDo: if not possible, try: left -> right -> below
        // if nothing is possible use (0|0)

        val layoutOffset: Vec2i

        // top
        layoutOffset = position - Vec2i(0, size.y + POSITION_OFFSET)
        if (!(layoutOffset isSmaller EMPTY)) {
            layoutOffset.x = minOf(maxOf(layoutOffset.x - size.x / 2 - POSITION_OFFSET, 0), windowSize.x - size.x) // try to center element, but clamp on edges (try not to make the popper go out of the window)
            this.layoutOffset = layoutOffset
            return
        }

        // failover
        this.layoutOffset = EMPTY
    }

    fun show() {
        guiRenderer.popper += this
    }

    fun hide() {
        dead = true
    }

    companion object {
        private val EMPTY = Vec2i.EMPTY
        private const val POSITION_OFFSET = 10
    }
}
