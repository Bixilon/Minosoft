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

package de.bixilon.minosoft.config.profile.profiles.rendering.camera

import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.primitive.DoubleDelegate
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile

class CameraC(profile: RenderingProfile) {

    /**
     * Field of view
     * Value must be greater than 0 and smaller than 180
     */
    var fov by DoubleDelegate(profile, 70.0, "profile.rendering.camera.fov", arrayOf(10.0..179.0))

    /**
     * Changes the fov to create a speed or slowness illusion
     */
    var dynamicFOV by BooleanDelegate(profile, true, "profile.rendering.camera.fov.dynamic")
}
