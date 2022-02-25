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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.container

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasSlot
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.items.ContainerItemsElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.Screen
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isGreater
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isSmaller
import glm_.vec2.Vec2i
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

abstract class ContainerScreen(
    guiRenderer: GUIRenderer,
    val container: Container,
    background: AtlasElement,
    items: Int2ObjectOpenHashMap<AtlasSlot> = background.slots,
) : Screen(guiRenderer), AbstractLayout<Element> {
    private val containerBackground = AtlasImageElement(guiRenderer, background)
    protected val containerElement = ContainerItemsElement(guiRenderer, container, items).apply { parent = this@ContainerScreen }
    override var activeElement: Element? = null
    override var activeDragElement: Element? = null

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)

        val centerOffset = offset + (size - containerBackground.size) / 2

        containerBackground.render(centerOffset, consumer, options)

        containerElement.render(centerOffset, consumer, options)
    }

    override fun forceSilentApply() {
        super.forceSilentApply()
        containerElement.apply()
    }

    override fun getAt(position: Vec2i): Pair<Element, Vec2i>? {
        val centerOffset = (size - containerBackground.size) / 2
        if (position isSmaller centerOffset) {
            return null
        }
        val offset = position - centerOffset
        if (offset isGreater containerElement.size) {
            return null
        }
        return Pair(containerElement, offset)
    }

    override fun onClose() {
        super.onClose()
        container.onClose()
    }
}
