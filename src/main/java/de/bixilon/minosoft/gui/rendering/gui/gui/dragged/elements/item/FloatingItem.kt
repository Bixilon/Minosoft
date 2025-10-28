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

package de.bixilon.minosoft.gui.rendering.gui.gui.dragged.elements.item

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.actions.types.DropFloatingContainerAction
import de.bixilon.minosoft.data.container.actions.types.SlotCounts
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.items.RawItemElement
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.Dragged
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer

class FloatingItem(
    guiRenderer: GUIRenderer,
    val stack: ItemStack,
    val container: Container? = null,
    size: Vec2f = RawItemElement.DEFAULT_SIZE,
) : Dragged(guiRenderer) {
    private val itemElement = RawItemElement(guiRenderer, size, stack, this)

    init {
        forceSilentApply()
        _size = size
    }

    override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {
        itemElement.render(offset, consumer, options)
    }

    override fun forceSilentApply() {
    }

    override fun onDragMouseAction(position: Vec2f, button: MouseButtons, action: MouseActions, count: Int, target: Element?) {
        if (action != MouseActions.PRESS) {
            return
        }
        if (button != MouseButtons.LEFT && button != MouseButtons.RIGHT) {
            return
        }
        if (target == null) {
            container?.execute(DropFloatingContainerAction(if (button == MouseButtons.LEFT) SlotCounts.ALL else SlotCounts.PART))
            guiRenderer.dragged.element = null
            return
        }
    }
}
