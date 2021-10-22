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

import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.RowLayout
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.max
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.copy
import glm_.vec2.Vec2i
import java.lang.Integer.max

class HotbarElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {
    val base = HotbarBaseElement(hudRenderer)
    val experience = HotbarExperienceBarElement(hudRenderer)
    val health = HotbarHealthElement(hudRenderer)
    val hunger = HotbarHungerElement(hudRenderer)
    val protection = HotbarProtectionElement(hudRenderer)
    val air = HotbarAirElement(hudRenderer)

    private val topLeft = RowLayout(hudRenderer, HorizontalAlignments.LEFT, 1) // contains health, protection, etc
    private val topRight = RowLayout(hudRenderer, HorizontalAlignments.RIGHT, 1) // contains hunger, air


    private val currentItemText = TextElement(hudRenderer, "", background = false, noBorder = true)
    private var nameShown = false
    private var nameShowTime = 0L
    private var lastItemStackNameShown: ItemStack? = null
    private var lastItemSlot = -1


    private var gamemode = hudRenderer.connection.player.tabListItem.gamemode

    private var renderElements = setOf(
        base,
        topLeft,
        topRight,
    )

    override var cacheEnabled: Boolean = false // ToDo: Cache correctly

    init {
        topLeft.apply {
            parent = this@HotbarElement
            spacing = VERTICAL_SPACING
            margin = margin.copy(left = HotbarBaseElement.HORIZONTAL_MARGIN)
        }
        topRight.apply {
            parent = this@HotbarElement
            spacing = VERTICAL_SPACING
            margin = margin.copy(right = HotbarBaseElement.HORIZONTAL_MARGIN)
        }

        topLeft += protection
        topLeft += health

        topRight += air
        topRight += hunger


        base.parent = this
        experience.parent = this
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        var maxZ = 0

        if (nameShown) {
            currentItemText.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, currentItemText.size.x), 0), z, consumer)
            offset.y += currentItemText.size.y + ITEM_NAME_OFFSET
        }

        if (gamemode.survival) {
            val topMaxSize = topLeft.size.max(topRight.size)
            maxZ = max(maxZ, topLeft.render(offset + Vec2i(0, VerticalAlignments.BOTTOM.getOffset(topMaxSize.y, topLeft.size.y)), z, consumer))
            maxZ = max(maxZ, topRight.render(offset + Vec2i(HorizontalAlignments.RIGHT.getOffset(size.x, topRight.size.x), VerticalAlignments.BOTTOM.getOffset(topMaxSize.y, topRight.size.y)), z, consumer))
            offset.y += topMaxSize.y + VERTICAL_SPACING

            maxZ = max(maxZ, experience.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, experience.size.x), 0), z, consumer))
            offset.y += experience.size.y + VERTICAL_SPACING
        }

        maxZ = max(maxZ, base.render(offset, z, consumer))

        return maxZ
    }

    override fun forceSilentApply() {
        for (element in renderElements) {
            element.silentApply()
        }

        val size = Vec2i(base.size)

        gamemode = hudRenderer.connection.player.tabListItem.gamemode
        if (gamemode.survival) {
            size.y += max(topLeft.size.y, topRight.size.y) + VERTICAL_SPACING

            size.y += experience.size.y + VERTICAL_SPACING
        }

        if (nameShown) {
            size.y += currentItemText.size.y + ITEM_NAME_OFFSET
            size.x = max(size.x, currentItemText.size.x)
        }

        _size = size
        cacheUpToDate = false
    }

    override fun silentApply(): Boolean {
        val itemSlot = hudRenderer.connection.player.selectedHotbarSlot
        val currentItem = hudRenderer.connection.player.inventory.getHotbarSlot(itemSlot)
        val time = System.currentTimeMillis()
        if (currentItem != lastItemStackNameShown || itemSlot != lastItemSlot) {
            currentItemText.text = hudRenderer.connection.player.inventory.getHotbarSlot()?.displayName ?: ""
            nameShowTime = time
            lastItemStackNameShown = currentItem
            lastItemSlot = itemSlot
            nameShown = true
        } else if (currentItem != null) {
            if (time - nameShowTime > 2500) {
                nameShown = false
            }
        }

        forceSilentApply() // ToDo: Check stuff
        return true
    }

    override fun onChildChange(child: Element) {
        silentApply() // ToDo: Check
        parent?.onChildChange(this)
    }

    override fun tick() {
        silentApply()
        base.tick()

        if (gamemode.survival) {
            topLeft.tick()
            topRight.tick()
        }
    }

    companion object {
        private const val ITEM_NAME_OFFSET = 5
        private const val VERTICAL_SPACING = 1
    }
}
