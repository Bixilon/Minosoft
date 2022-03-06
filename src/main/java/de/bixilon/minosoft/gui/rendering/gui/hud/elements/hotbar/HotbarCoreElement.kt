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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.RowLayout
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.max
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4iUtil.copy
import glm_.vec2.Vec2i
import java.lang.Integer.max

class HotbarCoreElement(guiRenderer: GUIRenderer) : Element(guiRenderer) {
    val base = HotbarBaseElement(guiRenderer)
    val experience = HotbarExperienceBarElement(guiRenderer)
    val health = HotbarHealthElement(guiRenderer)
    val hunger = HotbarHungerElement(guiRenderer)
    val protection = HotbarProtectionElement(guiRenderer)
    val air = HotbarAirElement(guiRenderer)
    val vehicleHealth = HotbarVehicleHealthElement(guiRenderer)

    private val topLeft = RowLayout(guiRenderer, HorizontalAlignments.LEFT, 1) // contains health, protection, etc
    private val topRight = RowLayout(guiRenderer, HorizontalAlignments.RIGHT, 1) // contains hunger, air


    private var gamemode = guiRenderer.renderWindow.connection.player.tabListItem.gamemode

    private var renderElements = setOf(
        base,
        experience,
        topLeft,
        topRight,
    )

    init {
        topLeft.apply {
            parent = this@HotbarCoreElement
            spacing = VERTICAL_SPACING
            margin = margin.copy(left = HotbarBaseElement.HORIZONTAL_MARGIN)
        }
        topRight.apply {
            parent = this@HotbarCoreElement
            spacing = VERTICAL_SPACING
            margin = margin.copy(right = HotbarBaseElement.HORIZONTAL_MARGIN)
        }

        topLeft += protection
        topLeft += health

        topRight += vehicleHealth // ToDo: Also show in creative
        topRight += air
        topRight += hunger


        base.parent = this
        experience.parent = this
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (gamemode.survival) {
            val topMaxSize = topLeft.size.max(topRight.size)
            topLeft.render(offset + Vec2i(0, VerticalAlignments.BOTTOM.getOffset(topMaxSize.y, topLeft.size.y)), consumer, options)
            topRight.render(offset + Vec2i(HorizontalAlignments.RIGHT.getOffset(size.x, topRight.size.x), VerticalAlignments.BOTTOM.getOffset(topMaxSize.y, topRight.size.y)), consumer, options)
            offset.y += topMaxSize.y + VERTICAL_SPACING

            experience.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, experience.size.x), 0), consumer, options)
            offset.y += experience.size.y + VERTICAL_SPACING

            base.render(offset, consumer, options)
        }
        // ToDo: Spectator hotbar
    }

    override fun forceSilentApply() {
        for (element in renderElements) {
            element.silentApply()
        }

        val size = Vec2i.EMPTY

        gamemode = guiRenderer.renderWindow.connection.player.tabListItem.gamemode
        if (gamemode.survival) {
            size += base.size
            size.y += max(topLeft.size.y, topRight.size.y) + VERTICAL_SPACING

            size.y += experience.size.y + VERTICAL_SPACING
        }

        _size = size
        cacheUpToDate = false
    }

    override fun silentApply(): Boolean {
        forceSilentApply() // ToDo: Check stuff
        return true
    }

    override fun onChildChange(child: Element) {
        silentApply() // ToDo: Check
        parent?.onChildChange(this)
    }

    override fun tick() {
        silentApply()

        if (gamemode.survival) {
            topLeft.tick()
            topRight.tick()
        }
        experience.tick()
        base.tick()
    }

    companion object {
        private const val VERTICAL_SPACING = 1
    }
}
