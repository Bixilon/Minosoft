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
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.Vec4Util.offset
import glm_.vec2.Vec2i
import java.lang.Integer.min
import kotlin.math.max

class GridLayout(hudRenderer: HUDRenderer, val grid: Vec2i) : Element(hudRenderer) {
    val columnConstraints: Array<GridColumnConstraint> = Array(grid.x) { GridColumnConstraint() }
    val rowConstraints: Array<GridRowConstraint> = Array(grid.y) { GridRowConstraint() }

    private val children: Array<Array<GridCell?>> = Array(grid.x) { Array(grid.y) { null } }
    private var columnStart = IntArray(grid.x)
    private var rowStart = IntArray(grid.y)

    override var cacheEnabled: Boolean = false // ToDo: Cache

    operator fun set(position: Vec2i, element: Element) = add(position, element)

    fun add(position: Vec2i, element: Element) {
        children[position.x][position.y]?.parent = null

        val cell = GridCell(hudRenderer, columnConstraints[position.x], rowConstraints[position.y], element, this)

        children[position.x][position.y] = cell

        apply()
    }

    init {
        apply()
    }


    override fun forceSilentApply() {
        // ToDo: This works with columns, but rows are not yet implemented

        // ToDo: Balance width of the columns

        /*
        Calculate new grid layout (sizes) with the new size of the child
        Set the parent (for maxSize)
        Apply the maxSize to the child
        Recalculate the grid to match the new size of the child?
         */

        // calculate width of every cell
        val width = IntArray(grid.x)

        for (x in 0 until grid.x) {
            for (y in 0 until grid.y) {
                val child = children[x][y] ?: continue
                width[x] = min(max(width[x], child.prefSize.x), columnConstraints[x].maxWidth)
            }
        }

        var alwaysGrowColumns = 0
        // Set the NEVER growing widths
        var availableWidth = maxSize.x
        for (x in 0 until grid.x) {
            val constraint = columnConstraints[x]
            val nextAvailable = availableWidth - width[x]
            if (constraint.grow == GridGrow.ALWAYS) {
                alwaysGrowColumns++
            }
            if (constraint.grow != GridGrow.NEVER) {
                continue
            }
            if (availableWidth != 0) {
                if (nextAvailable < 0) {
                    constraint.width = availableWidth
                    availableWidth = 0
                } else {
                    constraint.width = width[x]
                    availableWidth -= width[x]
                }
            }
        }

        // set ALWAYS growing widths (and split them)
        var remainingAlwaysGrowColumns = alwaysGrowColumns
        if (alwaysGrowColumns > 0) {
            for (x in 0 until grid.x) {
                val constraint = columnConstraints[x]
                if (constraint.grow != GridGrow.ALWAYS) {
                    continue
                }
                val widthFraction = availableWidth / remainingAlwaysGrowColumns--
                constraint.width = min(widthFraction, constraint.maxWidth)
                availableWidth -= constraint.width
            }
        }


        size = Vec2i(width.sum(), 0)

        // apply the size changes to all children
        applyOnlyChildren()


        // ToDo: Respect maxSize?
        val columnStart = IntArray(grid.x)
        // set the start offsets
        for (x in 1 until grid.x) {
            val offset = columnStart[x - 1]
            val previousWidth = columnConstraints[x - 1].width
            columnStart[x] = offset + previousWidth
        }
        this.columnStart = columnStart
    }


    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        var maxZ = 0
        for (x in 0 until grid.x) {
            for (y in 0 until grid.y) {
                val child = children[x][y] ?: continue
                val childZ = child.render(offset + margin.offset + Vec2i(columnStart[x] + columnConstraints[x].alignment.getOffset(columnConstraints[x].width, child.size.x), rowStart[y]), z, consumer)
                if (childZ > maxZ) {
                    maxZ = childZ
                }
            }
        }

        return maxZ
    }

    override fun tick() {
        super.tick()

        for (x in 0 until grid.x) {
            for (y in 0 until grid.y) {
                children[x][y]?.tick()
            }
        }
    }

    override fun silentApply(): Boolean {
        // ToDo: Check
        forceSilentApply()
        applyOnlyChildren()
        return true
    }

    private fun applyOnlyChildren() {
        for (x in 0 until grid.x) {
            for (y in 0 until grid.y) {
                children[x][y]?.silentApply()
            }
        }
    }

    override fun onChildChange(child: Element?) {
        apply()
        parent?.onChildChange(this)
    }
}
