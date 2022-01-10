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
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.AbstractGUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.GUIElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement

abstract class Screen(
    override val guiRenderer: AbstractGUIRenderer,
) : GUIElement {
    override val renderWindow: RenderWindow = guiRenderer.renderWindow
    val background = ImageElement(guiRenderer, renderWindow.WHITE_TEXTURE, size = guiRenderer.scaledSize, tint = RGBColor(1.0f, 1.0f, 1.0f, 0.8f))
}
