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

package de.bixilon.minosoft.gui.rendering.font.renderer

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh

object BaseComponentRenderer : ChatComponentRenderer<BaseComponent> {

    override fun render(initialOffset: Vec2i, offset: Vec2i, size: Vec2i, element: Element, renderWindow: RenderWindow, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, renderInfo: TextRenderInfo, text: BaseComponent): Boolean {
        for (part in text.parts) {
            if (ChatComponentRenderer.render(initialOffset, offset, size, element, renderWindow, consumer, options, renderInfo, part)) {
                return true
            }
        }
        return false
    }

    override fun render3DFlat(matrix: Mat4, mesh: WorldMesh, text: BaseComponent) {
        for (part in text.parts) {
            ChatComponentRenderer.render3DFlat(matrix, mesh, text)
        }
    }
}
