/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud

import com.squareup.moshi.Json
import de.bixilon.minosoft.data.registries.ResourceLocation
import glm_.vec2.Vec2

data class HUDElementProperties(
    val position: Vec2,
    @Json(name = "x_binding") val xBinding: PositionBindings = PositionBindings.FURTHEST_POINT_AWAY,
    @Json(name = "y_binding") val yBinding: PositionBindings = PositionBindings.FURTHEST_POINT_AWAY,
    @Json(name = "toggle_key_binding") var toggleKeyBinding: ResourceLocation? = null,
    val scale: Float = 1.0f,
    var enabled: Boolean = true,
    val properties: MutableMap<String, Any> = mutableMapOf(),
) {

    enum class PositionBindings {
        CENTER,
        FURTHEST_POINT_AWAY
    }
}
