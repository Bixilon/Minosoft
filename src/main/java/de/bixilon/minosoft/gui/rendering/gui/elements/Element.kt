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

package de.bixilon.minosoft.gui.rendering.gui.elements

import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.MAX
import glm_.vec2.Vec2i

abstract class Element {
    open var parent: Element? = null
    open var prepared: Boolean = false

    open var minSize: Vec2i = Vec2i.EMPTY
    open var prefMaxSize: Vec2i = Vec2i.MAX
    open var size: Vec2i = Vec2i()


    open val maxSize: Vec2i
        get() {
            val ret = Vec2i.MAX

            parent?.let {
                ret.x = it.maxSize.x
                ret.y = it.maxSize.y
            }

            val maxSize = Vec2i(prefMaxSize)

            if (maxSize.x < ret.x) {
                ret.x = maxSize.x
            }
            if (maxSize.y < ret.y) {
                ret.y = maxSize.y
            }

            return ret
        }

    /**
     * @return The number of z layers used
     */
    abstract fun render(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int


    open fun childChange(child: Element) {}
}
