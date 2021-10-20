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

import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.RowLayout
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.max
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
        }
        topRight.apply {
            parent = this@HotbarElement
            spacing = VERTICAL_SPACING
        }

        topLeft += protection
        topLeft += health

        topRight += air
        topRight += hunger

        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        var maxZ = 0


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

        _size = size
        cacheUpToDate = false
    }

    override fun silentApply(): Boolean {
        forceSilentApply()
        return true
    }

    override fun onChildChange(child: Element) {
        silentApply() // ToDo: Check
        parent?.onChildChange(this)
    }

    override fun tick() {
        super.tick()

        for (element in renderElements) {
            element.tick()
        }
    }

    companion object {
        private const val VERTICAL_SPACING = 1
    }
}
