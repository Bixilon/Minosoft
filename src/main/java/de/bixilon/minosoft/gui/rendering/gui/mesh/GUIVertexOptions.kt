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

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.minosoft.data.text.formatting.color.RGBColor

class GUIVertexOptions(
    val tintColor: RGBColor? = null,
    val alpha: Float = 1.0f,
) {
    companion object {

        fun GUIVertexOptions?.copy(tintColor: RGBColor? = null, alpha: Float = 1.0f): GUIVertexOptions? {
            if (this == null) return GUIVertexOptions(tintColor, alpha)
            var outColor = this.tintColor
            if (tintColor != null) {
                outColor = outColor?.mix(tintColor) ?: tintColor
            }
            val outAlpha = this.alpha * alpha
            if (outColor == null && outAlpha == 1.0f) return null


            return GUIVertexOptions(outColor, alpha = outAlpha)
        }
    }
}
