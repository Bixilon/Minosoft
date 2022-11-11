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

package de.bixilon.minosoft.config.profile.profiles.rendering.light

import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager.delegate

class LightC {

    /**
     * Changes the gamma value of the light map
     * In original minecraft this setting is called brightness
     * Must be non-negative and may not exceed 1
     */
    var gamma by delegate(0.0f) { check(it in 0.0f..1.0f) { "Gamma must be non-negative and <= 1" } }

    /**
     * Makes everything bright
     */
    var fullbright by delegate(false)
}
