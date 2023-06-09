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

package de.bixilon.minosoft.gui.rendering.font.renderer.code

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.font.WorldGUIConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

interface CodePointRenderer {

    fun calculateWidth(scale: Float, shadow: Boolean): Float

    fun render(position: Vec2, color: RGBColor, shadow: Boolean, bold: Boolean, italic: Boolean, scale: Float, consumer: GUIVertexConsumer, options: GUIVertexOptions?)

    fun render3d(color: RGBColor, shadow: Boolean, bold: Boolean, italic: Boolean, scale: Float, consumer: WorldGUIConsumer): Float {
        render(Vec2.EMPTY, color, shadow, bold, italic, scale, consumer, null)

        return calculateWidth(scale, shadow)
    }
}
