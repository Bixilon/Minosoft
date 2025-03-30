/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.camera.fog

import de.bixilon.minosoft.data.text.formatting.color.RGBAColor

data class FogState(
    var enabled: Boolean = true,
    var start: Float = 0.0f,
    var end: Float = 0.0f,
    var color: RGBAColor? = null,
) {
    var revision = -1


    fun flags(): Int {
        var flags = 0
        if (enabled) {
            flags = flags or FogFlags.ENABLE.mask
        }
        if (color != null) {
            flags = flags or FogFlags.USE_COLOR.mask
        }

        return flags
    }
}
