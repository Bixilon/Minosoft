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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.chat

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.collection.SetChildrenManager
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextFlowElement
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.util.Initializable

abstract class AbstractChatElement(guiRenderer: GUIRenderer) : Element(guiRenderer), Initializable, Drawable, AbstractLayout<Element> {
    override val children = SetChildrenManager(this)
    protected val connection = context.connection
    protected val profile = connection.profiles.gui
    protected val messages = TextFlowElement(guiRenderer, 20000).apply { parent = this@AbstractChatElement }
    override var activeElement: Element? = null
    override var activeDragElement: Element? = null

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        messages.render(offset + Vec2i(ChatElement.CHAT_INPUT_MARGIN, 0), consumer, options)
    }

    override fun onScroll(position: Vec2, scrollOffset: Vec2): Boolean {
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
