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

import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.MAX
import glm_.vec2.Vec2i

class InfiniteSizeElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {
    override var parent: Element?
        get() = null
        set(value) {}

    override var maxSize: Vec2i
        get() = Vec2i.MAX
        set(value) {}

    override var prefSize: Vec2i
        get() = Vec2i.MAX
        set(value) {}

    override fun render(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        error("Can not render InfiniteSizeElement")
    }

    override fun silentApply() = Unit

    override fun apply() = Unit
}
