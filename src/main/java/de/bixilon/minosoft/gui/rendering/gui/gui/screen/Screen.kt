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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import glm_.vec2.Vec2i

abstract class Screen(
    guiRenderer: GUIRenderer,
) : Element(guiRenderer), LayoutedElement {
    protected val background = AtlasImageElement(guiRenderer, renderWindow.WHITE_TEXTURE, size = guiRenderer.scaledSize, tint = RGBColor(0.0f, 0.0f, 0.0f, 0.8f))
    override val layoutOffset: Vec2i = Vec2i(0, 0)

    override var _size: Vec2i
        get() = guiRenderer.scaledSize
        set(value) {}

    override fun forceSilentApply() {
        background.size = guiRenderer.scaledSize
        _size = guiRenderer.scaledSize
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        background.render(offset, consumer, options)
    }
}
