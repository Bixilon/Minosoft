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
import de.bixilon.minosoft.gui.rendering.gui.elements.ElementAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.ElementAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import glm_.vec2.Vec2i
import java.lang.Integer.max

class HotbarElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {
    private val base = HotbarBaseElement(hudRenderer)
    private val health = HotbarHealthElement(hudRenderer)
    private val hunger = HotbarHungerElement(hudRenderer)


    private var elements = setOf(
        base,
        health,
        hunger,
    )

    override var cacheEnabled: Boolean = false

    init {
        silentApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        silentApply()
        val initialOffset = Vec2i(offset)
        var maxZ = 0
        maxZ = max(maxZ, health.render(offset, z, consumer))
        hunger.render(offset + Vec2i(ElementAlignments.RIGHT.getOffset(size.x, hunger.size.x), 0), z, consumer) // ToDo
        offset.y += health.size.y
        maxZ = max(maxZ, base.render(offset, z, consumer))

        return maxZ
    }

    override fun silentApply() {
        for (element in elements) {
            element.silentApply()
        }

        size = base.size + Vec2i(0, health.size.y)
    }

    override fun tick() {
        super.tick()

        for (element in elements) {
            element.tick()
        }
    }
}
