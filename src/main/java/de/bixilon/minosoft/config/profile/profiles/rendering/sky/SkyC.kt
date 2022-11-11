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

package de.bixilon.minosoft.config.profile.profiles.rendering.sky

import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.rendering.sky.cloud.CloudC

class SkyC {

    /**
     * Biomes to query when calculating sky color
     */
    var biomeRadius by delegate(3) { check(it in 0..5) { "Must be in range 0..5" } }

    /**
     * Enables or disables the (ugly?) sun scatter
     */
    var sunScatter by delegate(true)

    val clouds = CloudC()
}
