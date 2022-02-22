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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.chat

import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextFlowElement
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.renderer.Drawable
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i

abstract class AbstractChatElement(guiRenderer: GUIRenderer) : Element(guiRenderer), LayoutedElement, Initializable, Drawable, AbstractLayout<Element> {
    protected val connection = renderWindow.connection
    protected val profile = connection.profiles.gui
    protected val messages = TextFlowElement(guiRenderer, 20000).apply { parent = this@AbstractChatElement }
    override var activeElement: Element? = null
    override var activeDragElement: Element? = null

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        messages.render(offset + Vec2i(ChatElement.CHAT_INPUT_MARGIN, 0), consumer, options)
    }

    override fun onScroll(position: Vec2i, scrollOffset: Vec2d): Boolean {
        val size = messages.size
        if (position.y > size.y || position.x > messages.size.x) {
            return false
        }
        messages.onScroll(position, scrollOffset)
        return true
    }

    override fun tick() {
        messages.tick()
    }
}
