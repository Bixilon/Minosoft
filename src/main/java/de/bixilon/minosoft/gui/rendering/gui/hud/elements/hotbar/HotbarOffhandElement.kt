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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.minosoft.data.player.Arms
import de.bixilon.minosoft.data.player.Arms.Companion.opposite
import de.bixilon.minosoft.gui.rendering.gui.AbstractGUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.items.ContainerItemsElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4iUtil.marginOf
import glm_.vec2.Vec2i
import java.lang.Integer.max

class HotbarOffhandElement(guiRenderer: AbstractGUIRenderer) : Element(guiRenderer) {
    private val frames = arrayOf(
        guiRenderer.renderWindow.atlasManager["minecraft:offhand_right_arm_frame"],
        guiRenderer.renderWindow.atlasManager["minecraft:offhand_left_arm_frame"],
    )

    val offArm = guiRenderer.renderWindow.connection.player.mainArm.opposite // ToDo: Support arm change
    private val frame = frames[offArm.ordinal]!!

    private var frameImage = ImageElement(guiRenderer, frame)
    private val containerElement = ContainerItemsElement(guiRenderer, guiRenderer.renderWindow.connection.player.inventory, frame.slots)

    init {
        _size = frame.size
        val margin = if (offArm == Arms.LEFT) {
            marginOf(right = 5)
        } else {
            marginOf(left = 5)
        }
        this.margin = margin
    }


    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        var maxZ = frameImage.render(offset, z, consumer, options)
        maxZ = max(maxZ, containerElement.render(offset, z + maxZ, consumer, options))

        return maxZ
    }

    override fun silentApply(): Boolean {
        val container = containerElement.silentApply()

        if (super.silentApply()) {
            return true
        }
        if (container) {
            forceSilentApply()
            return true
        }
        return false
    }

    override fun forceSilentApply() {
        cacheUpToDate = false
    }
}
