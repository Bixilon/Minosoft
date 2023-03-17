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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.container

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasSlot
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.items.ContainerItemsElement
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.Screen
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isGreater
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

abstract class ContainerScreen<C : Container>(
    guiRenderer: GUIRenderer,
    val container: C,
    items: Int2ObjectOpenHashMap<AtlasSlot>,
) : Screen(guiRenderer), AbstractLayout<Element> {
    protected open val containerElement = ContainerItemsElement(guiRenderer, container, items).apply { parent = this@ContainerScreen }
    override var activeElement: Element? = null
    override var activeDragElement: Element? = null
    protected open val customRenderer: Boolean = false

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)
        if (customRenderer) {
            return
        }
        forceRenderContainerScreen(offset, consumer, options)
    }

    protected open fun forceRenderContainerScreen(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        containerElement.render(offset, consumer, options)
    }

    override fun forceSilentApply() {
        super.forceSilentApply()
        containerElement.apply()
    }

    override fun getAt(position: Vec2i): Pair<Element, Vec2i>? {
        if (position isGreater containerElement.size) {
            return null
        }
        return Pair(containerElement, position)
    }

    override fun onOpen() {
        guiRenderer.connection.player.items.opened = container
    }

    override fun onClose() {
        super.onClose()
        container.close()
    }
}
