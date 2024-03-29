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

package de.bixilon.minosoft.config.profile.profiles.controls.mouse

import de.bixilon.minosoft.config.profile.delegate.primitive.FloatDelegate
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfile

class MouseC(profile: ControlsProfile) {

    /**
     * Mouse sensitivity in percent
     * Controls how fast the mouse rotates the player around
     * Must be non-negative
     */
    var sensitivity by FloatDelegate(profile, 1.0f, arrayOf(0.01f..10.0f))

    /**
     * Controls how fast you scroll (e.g. in the hotbar)
     * Must be non-negative
     */
    var scrollSensitivity by FloatDelegate(profile, 1.0f, arrayOf(0.01f..10.0f))
}
