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

package de.bixilon.minosoft.gui.rendering.gui.elements.layout.grid

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec4.Vec4i
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMeshCache
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

class GridCell(
    guiRenderer: GUIRenderer,
    private val columnConstraint: GridColumnConstraint,
    private val rowConstraint: GridRowConstraint,
    private val child: Element,
    parent: Element?,
) : Element(guiRenderer, 0) {
    override var cacheUpToDate: Boolean by child::cacheUpToDate
    override var cacheEnabled: Boolean by child::cacheEnabled
    override var prefMaxSize: Vec2i by child::prefMaxSize
    override var size: Vec2i by child::size
    override var margin: Vec4i by child::margin
    override var prefSize: Vec2i by child::prefSize
    override val cache: GUIMeshCache by child::cache

    init {
        _parent = parent
    }

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

    override fun render(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        return child.render(offset, consumer, options)
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        return child.forceRender(offset, consumer, options)
    }

    override fun tick() {
        child.tick()
    }

    override fun silentApply(): Boolean {
        return child.silentApply()
    }

    @Suppress("DEPRECATION")
    override fun forceSilentApply() {
        child.forceSilentApply()
    }

    override fun apply() {
        child.apply()
    }

    override fun forceApply() {
        child.forceApply()
    }

    override fun onChildChange(child: Element) {
        super.onChildChange(this)
    }
}
