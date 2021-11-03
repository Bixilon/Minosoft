/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.config.game

import com.squareup.moshi.Json

data class CameraGameConfig(
    @Json(name = "render_distance") var renderDistance: Int = 10,
    var fov: Double = 60.0,
    @Json(name = "dynamic_fov") var dynamicFov: Boolean = true,
    @Json(name = "no_clip_movement") var noCipMovement: Boolean = false,
    @Json(name = "disable_movement_sending") var disableMovementSending: Boolean = true, // ToDo: This should get false once the physics are "complete"
)
