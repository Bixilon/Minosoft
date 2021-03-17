/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.textures.properties

import com.squareup.moshi.Json
import de.bixilon.minosoft.gui.rendering.textures.Texture

data class AnimationProperties(
    val interpolate: Boolean = false,
    var width: Int = -1,
    var height: Int = -1,
    @Json(name = "frametime") val frameTime: Int = 1,
    val frames: Any = Any(),// ToDo,
) {
    var animationId = -1

    var frameCount = -1

    fun postInit(texture: Texture) {
        if (width == -1) {
            width = texture.size.x
        }
        if (height == -1) {
            height = texture.size.x // That's correct!
        }

        frameCount = texture.size.y / height
    }
}
