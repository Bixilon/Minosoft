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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.container

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasSlot
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.isSmaller
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

abstract class BackgroundedContainerScreen<C : Container>(
    guiRenderer: GUIRenderer,
    container: C,
    protected val atlasElement: AtlasElement?,
    items: Int2ObjectOpenHashMap<AtlasSlot> = atlasElement?.slots ?: Int2ObjectOpenHashMap(),
) : ContainerScreen<C>(guiRenderer, container, items) {
    protected val containerBackground = AtlasImageElement(guiRenderer, atlasElement)
    override val customRenderer: Boolean get() = true

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val centerOffset = offset + (size - containerBackground.size) / 2
        super.forceRender(centerOffset, consumer, options)
        containerBackground.render(centerOffset, consumer, options)
        forceRenderContainerScreen(centerOffset, consumer, options)
    }

    override fun getAt(position: Vec2): Pair<Element, Vec2>? {
        val centerOffset = (size - containerBackground.size) / 2
        if (position isSmaller centerOffset) {
            return null
        }
        val start = position - centerOffset

        return getContainerAt(start) ?: super.getAt(position - centerOffset)
    }

    protected open fun getContainerAt(position: Vec2): Pair<Element, Vec2>? = null
}
