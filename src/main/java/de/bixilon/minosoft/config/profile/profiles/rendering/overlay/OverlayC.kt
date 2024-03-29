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

package de.bixilon.minosoft.config.profile.profiles.rendering.overlay

import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile
import de.bixilon.minosoft.config.profile.profiles.rendering.overlay.arm.ArmC
import de.bixilon.minosoft.config.profile.profiles.rendering.overlay.fire.FireC
import de.bixilon.minosoft.config.profile.profiles.rendering.overlay.weather.WeatherC

class OverlayC(profile: RenderingProfile) {
    /**
     * Enables the powder snow 2d overlay if the player is frozen
     */
    var powderSnow by BooleanDelegate(profile, true)

    /**
     * Enabled the pumpkin blur overlay if the player is waring a carved pumpkin
     */
    var pumpkin by BooleanDelegate(profile, true)

    /**
     * Enables the world boreder overlay
     */
    var worldBorder by BooleanDelegate(profile, true)

    val fire = FireC(profile)
    val weather = WeatherC(profile)
    val arm = ArmC(profile)
}
