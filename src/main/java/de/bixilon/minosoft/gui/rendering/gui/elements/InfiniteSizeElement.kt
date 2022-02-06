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

package de.bixilon.minosoft.gui.rendering.gui.elements

import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.MAX
import glm_.vec2.Vec2i

class InfiniteSizeElement(guiRenderer: GUIRenderer) : Element(guiRenderer) {
    override var parent: Element?
        get() = null
        set(value) = Unit

    override var maxSize: Vec2i
        get() = Vec2i.MAX
        set(value) = Unit

    override var prefSize: Vec2i
        get() = Vec2i.MAX
        set(value) = Unit

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        error("Can not render InfiniteSizeElement")
    }

    override fun forceSilentApply() = Unit
    override fun silentApply(): Boolean = false

    override fun apply() = Unit
}
