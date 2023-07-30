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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.horizontal
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.offset

class GridLayout(guiRenderer: GUIRenderer, val grid: Vec2i) : Element(guiRenderer) {
    val columnConstraints: Array<GridColumnConstraint> = Array(grid.x) { GridColumnConstraint() }
    val rowConstraints: Array<GridRowConstraint> = Array(grid.y) { GridRowConstraint() }

    private val children: Array<Array<GridCell?>> = Array(grid.x) { arrayOfNulls(grid.y) }
    private var columnStart = FloatArray(grid.x)
    private var rowStart = FloatArray(grid.y)

    override var cacheEnabled: Boolean
        get() = super.cacheEnabled
        set(value) {
            for (array in children) {
                for (child in array) {
                    if ((child ?: continue).cacheEnabled) {
                        super.cacheEnabled = false
                        return
                    }
                }
            }
            super.cacheEnabled = true
        }

    operator fun set(position: Vec2i, element: Element) = add(position, element)

    fun add(position: Vec2i, element: Element) {
        children[position.x][position.y]?.parent = null

        val cell = GridCell(guiRenderer, columnConstraints[position.x], rowConstraints[position.y], element, this)

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
        val width = FloatArray(grid.x)

        for (x in 0 until grid.x) {
            for (y in 0 until grid.y) {
                val child = children[x][y] ?: continue
                width[x] = minOf(maxOf(width[x], child.prefSize.x + child.margin.horizontal), columnConstraints[x].maxWidth)
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
            if (availableWidth != 0.0f) {
                if (nextAvailable < 0) {
                    constraint.width = availableWidth
                    availableWidth = 0.0f
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
                constraint.width = minOf(widthFraction, constraint.maxWidth)
                availableWidth -= constraint.width
            }
        }


        _size = Vec2(width.sum(), 0.0f)

        // apply the size changes to all children
        applyOnlyChildren()


        // ToDo: Respect maxSize?
        val columnStart = FloatArray(grid.x)
        // set the start offsets
        for (x in 1 until grid.x) {
            val offset = columnStart[x - 1]
            val previousWidth = columnConstraints[x - 1].width
            columnStart[x] = offset + previousWidth
        }
        this.columnStart = columnStart
        cacheUpToDate = false
    }


    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        for (x in 0 until grid.x) {
            for (y in 0 until grid.y) {
                val child = children[x][y] ?: continue
                child.render(offset + margin.offset + Vec2(columnStart[x] + columnConstraints[x].alignment.getOffset(columnConstraints[x].width, child.size.x), rowStart[y]), consumer, options)
            }
        }
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
        // ToDo: Check for changes
        if (!super.silentApply()) {
            forceSilentApply()
        }
        return true
    }

    private fun applyOnlyChildren() {
        for (x in 0 until grid.x) {
            for (y in 0 until grid.y) {
                children[x][y]?.silentApply()
            }
        }
    }

    override fun onChildChange(child: Element) {
        apply()
        parent?.onChildChange(this)
    }
}
