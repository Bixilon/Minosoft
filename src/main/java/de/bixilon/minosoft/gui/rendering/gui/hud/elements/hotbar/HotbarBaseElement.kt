/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.data.registries.other.containers.PlayerInventory
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.items.ContainerItemsElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class HotbarBaseElement(hudRenderer: HUDRenderer) : Element(hudRenderer), Pollable {
    private val baseAtlasElement = hudRenderer.atlasManager[BASE]!!
    private val base = ImageElement(hudRenderer, baseAtlasElement)
    private val frame = ImageElement(hudRenderer, hudRenderer.atlasManager[FRAME]!!, size = Vec2i(FRAME_SIZE))

    private val inventoryElement = ContainerItemsElement(hudRenderer, hudRenderer.connection.player.inventory, baseAtlasElement.slots)

    private var selectedSlot = 0

    init {
        size = HOTBAR_BASE_SIZE + Vec2i(HORIZONTAL_MARGIN * 2, 1) // offset left and right; offset for the frame is just on top, not on the bottom
        cacheUpToDate = false // ToDo: Check changes
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        base.render(offset + HORIZONTAL_MARGIN, z, consumer)

        baseAtlasElement.slots[selectedSlot + PlayerInventory.HOTBAR_OFFSET]?.let {
            frame.render(offset + it.start - HORIZONTAL_MARGIN + FRAME_OFFSET, z + 2, consumer)
        }

        inventoryElement.render(offset, z, consumer)

        // ToDo: Item rendering

        return 2 // bar + frame
    }

    override fun poll(): Boolean {
        val selectedSlot = hudRenderer.connection.player.selectedHotbarSlot

        if (this.selectedSlot != selectedSlot) {
            this.selectedSlot = selectedSlot
            return true
        }

        return false
    }

    override fun forceSilentApply() {
        cacheUpToDate = false
    }

    override fun tick() {
        apply()
    }

    companion object {
        private val BASE = "minecraft:hotbar_base".toResourceLocation()
        private val FRAME = "minecraft:hotbar_frame".toResourceLocation()

        private val HOTBAR_BASE_SIZE = Vec2i(182, 22)
        private const val FRAME_SIZE = 24
        private const val HORIZONTAL_MARGIN = 1
        private const val FRAME_OFFSET = -2 // FRAME_SIZE - HOTBAR_BASE_SIZE.y
    }
}
