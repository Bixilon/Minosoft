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

import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.ChildAlignable

class GridColumnConstraint(
    var prefWidth: Float = 0.0f,
    var maxWidth: Float = Float.MAX_VALUE,
    var grow: GridGrow = GridGrow.ALWAYS,
    var alignment: HorizontalAlignments = HorizontalAlignments.LEFT,
) : ChildAlignable {
    override var childAlignment: HorizontalAlignments by this::alignment

    var width: Float = 0.0f
}
