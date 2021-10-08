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

package de.bixilon.minosoft.gui.rendering.gui.elements.layout.grid

import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i

class GridCell(
    hudRenderer: HUDRenderer,
    private val columnConstraint: GridColumnConstraint,
    private val rowConstraint: GridRowConstraint,
    private val child: Element,
    override var parent: Element?,
) : Element(hudRenderer) {
    override var cacheUpToDate: Boolean by child::cacheUpToDate
    override var cacheEnabled: Boolean by child::cacheEnabled
    override var initialCacheSize: Int by child::initialCacheSize
    override var prefMaxSize: Vec2i by child::prefMaxSize
    override var size: Vec2i by child::size
    override var margin: Vec4i by child::margin
    override var prefSize: Vec2i by child::prefSize

    override val maxSize: Vec2i
        get() {
            val maxSize = Vec2i(super.maxSize)

            if (columnConstraint.width < maxSize.x) {
                maxSize.x = columnConstraint.width
            }
            if (rowConstraint.height < maxSize.y) {
                maxSize.y = rowConstraint.height
            }

            return maxSize
        }

    init {
        child.parent = this
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        return child.render(offset, z, consumer)
    }

    override fun tick() {
        child.tick()
    }

    override fun checkSilentApply() {
        child.checkSilentApply()
    }

    override fun silentApply() {
        child.silentApply()
    }

    override fun apply() {
        child.apply()
    }
}
