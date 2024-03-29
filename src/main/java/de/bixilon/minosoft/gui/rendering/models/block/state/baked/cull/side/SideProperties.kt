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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side

import com.google.common.base.Objects
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies

class SideProperties(
    val faces: Array<FaceProperties>,
    val transparency: TextureTransparencies?,
) {
    init {
        if (faces.isEmpty()) throw IllegalCallerException("properties is empty!")
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SideProperties) return false
        return faces.contentEquals(other.faces) && transparency == other.transparency
    }

    override fun hashCode(): Int {
        return Objects.hashCode(faces.contentHashCode(), transparency)
    }
}
