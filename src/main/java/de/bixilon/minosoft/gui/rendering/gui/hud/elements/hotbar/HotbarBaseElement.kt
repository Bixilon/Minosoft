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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas.Companion.get
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.items.ContainerItemsElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class HotbarBaseElement(guiRenderer: GUIRenderer) : Element(guiRenderer), Pollable {
    private val atlas = guiRenderer.atlas[ATLAS]
    private val baseAtlasElement = atlas["base"]
    private val base = AtlasImageElement(guiRenderer, baseAtlasElement)
    private val frame = AtlasImageElement(guiRenderer, atlas["frame"], size = Vec2i(FRAME_SIZE))

    private val containerElement = ContainerItemsElement(guiRenderer, guiRenderer.context.connection.player.items.inventory, baseAtlasElement?.slots ?: Int2ObjectOpenHashMap())

    private var selectedSlot = 0

    init {
        size = HOTBAR_BASE_SIZE + Vec2(HORIZONTAL_MARGIN * 2, 1) // offset left and right; offset for the frame is just on top, not on the bottom
        cacheUpToDate = false // ToDo: Check changes

        base.parent = this
        frame.parent = this
        containerElement.parent = this
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        base.render(offset + HORIZONTAL_MARGIN, consumer, options)

        baseAtlasElement?.slots?.get(selectedSlot + PlayerInventory.HOTBAR_OFFSET)?.let {
            frame.render(offset + it.start - HORIZONTAL_MARGIN + FRAME_OFFSET, consumer, options)
        }

        containerElement.render(offset + HORIZONTAL_MARGIN, consumer, options)
    }

    override fun poll(): Boolean {
        val selectedSlot = guiRenderer.context.connection.player.items.hotbar

        if (this.selectedSlot != selectedSlot || containerElement.silentApply()) {
            this.selectedSlot = selectedSlot
            return true
        }

        return false
    }

    override fun forceSilentApply() {
        containerElement.silentApply()
        cacheUpToDate = false
    }

    companion object {
        val ATLAS = minecraft("gui/hotbar/hotbar")

        private val HOTBAR_BASE_SIZE = Vec2(182, 22)
        private const val FRAME_SIZE = 24
        const val HORIZONTAL_MARGIN = 1.0f
        private const val FRAME_OFFSET = -2 // FRAME_SIZE - HOTBAR_BASE_SIZE.y
    }
}
