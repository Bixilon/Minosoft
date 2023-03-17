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

package de.bixilon.minosoft.config.profile.profiles.rendering.camera.shaking

import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.primitive.FloatDelegate
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile

class ShakingC(profile: RenderingProfile) {
    /**
     * This enables camera shaking
     */
    var enabled by BooleanDelegate(profile, true)

    var amplifier by FloatDelegate(profile, 1.0f, "", ranges = arrayOf(0.1f..2.0f))

    /**
     * Shake the camera while walking
     */
    var walking by BooleanDelegate(profile, true)

    /**
     * Shake the camera when talking damage
     */
    var damage by BooleanDelegate(profile, true)
}
