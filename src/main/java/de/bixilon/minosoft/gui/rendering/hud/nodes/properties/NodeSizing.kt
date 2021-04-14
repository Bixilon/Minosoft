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

package de.bixilon.minosoft.gui.rendering.hud.nodes.properties

import de.bixilon.minosoft.util.MMath
import glm_.vec2.Vec2i

data class NodeSizing(
    var minSize: Vec2i = Vec2i(0, 0),
    var margin: Spacing = Spacing(0, 0, 0, 0),
    var padding: Spacing = Spacing(0, 0, 0, 0),
    var maxSize: Vec2i = Vec2i(Int.MAX_VALUE, Int.MAX_VALUE),
    var forceAlign: NodeAlignment? = null,
) {
    var currentSize: Vec2i = Vec2i(minSize)


    fun validate() {
        MMath.clamp(currentSize, minSize, maxSize)
    }

    var forceSize: Vec2i
        get() = currentSize
        set(value) {
            minSize = value
            maxSize = value
            validate()
        }
}
