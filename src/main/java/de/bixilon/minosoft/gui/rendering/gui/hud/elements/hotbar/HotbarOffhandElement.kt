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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.data.entities.entities.player.Arms.Companion.opposite
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.items.ContainerItemsElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.marginOf

class HotbarOffhandElement(guiRenderer: GUIRenderer) : Element(guiRenderer) {
    private val frames = arrayOf(
        guiRenderer.atlasManager["minecraft:offhand_right_arm_frame"],
        guiRenderer.atlasManager["minecraft:offhand_left_arm_frame"],
    )

    val offArm = guiRenderer.context.connection.player.mainArm.opposite // ToDo: Support arm change
    private val frame = frames[offArm.ordinal]!!

    private var frameImage = AtlasImageElement(guiRenderer, frame)
    private val containerElement = ContainerItemsElement(guiRenderer, guiRenderer.context.connection.player.items.inventory, frame.slots)

    init {
        this.size = Vec2(frame.size)
        val margin = if (offArm == Arms.LEFT) {
            marginOf(right = 5.0f)
        } else {
            marginOf(left = 5.0f)
        }
        this.margin = margin
        containerElement.parent = this
    }


    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        frameImage.render(offset, consumer, options)
        containerElement.render(offset, consumer, options)
    }
}
