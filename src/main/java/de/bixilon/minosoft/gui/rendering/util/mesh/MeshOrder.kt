/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.util.mesh

import de.bixilon.minosoft.gui.rendering.system.base.RenderOrder

// TODO: Replace mesh order with index buffer
object MeshOrder {
    @Deprecated("legacy")
    val LEGACY_QUAD = RenderOrder(intArrayOf(
        0, 1,
        3, 2,
        2, 3,
        1, 0,
    ))

    @Deprecated("legacy")
    val LEGACY_TRIANGLE = RenderOrder(intArrayOf(
        // TOOD: they are all rotated 90° wrong, fix this for triangle and quad order
        0, 1,
        3, 2,
        2, 3,
        2, 3,
        1, 0,
        0, 1,
    ))

    val TRIANGLE = RenderOrder(intArrayOf(
        0, 0,
        3, 3,
        2, 2,
        2, 2,
        1, 1,
        0, 0,
    ))
    val QUAD = RenderOrder(intArrayOf(
        0, 0,
        3, 3,
        2, 2,
        1, 1,
    ))
}
