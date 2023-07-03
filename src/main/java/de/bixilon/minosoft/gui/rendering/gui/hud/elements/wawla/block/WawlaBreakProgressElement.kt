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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla.block

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.input.interaction.breaking.survival.BlockBreakProductivity

class WawlaBreakProgressElement(block: BlockWawlaElement) : Element(block.guiRenderer) {
    private val breaking = context.connection.camera.interactions.breaking
    private val status = breaking.digging.status

    init {
        parent = block
        tryUpdate()
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (status == null) {
            return
        }
        val maxWidth = parent?.size?.x ?: 0.0f
        if (status.productivity == BlockBreakProductivity.USELESS) {
            ColorElement(guiRenderer, Vec2(maxWidth, size.y), color = ChatColors.RED).forceRender(offset, consumer, options)
            return
        }
        val width = (status.progress * (maxWidth - 1)).toInt() + 1 // bar is always 1 pixel wide

        val color = when (status.productivity) {
            BlockBreakProductivity.INEFFECTIVE -> ChatColors.YELLOW
            else -> ChatColors.GREEN
        }

        ColorElement(guiRenderer, Vec2(width, size.y), color).render(offset, consumer, options)
    }

    override fun update() {
        this.size = if (status == null) Vec2.EMPTY else Vec2(-1, 3)
    }
}
