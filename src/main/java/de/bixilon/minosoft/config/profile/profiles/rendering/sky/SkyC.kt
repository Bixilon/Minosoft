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

import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.primitive.IntDelegate
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile
import de.bixilon.minosoft.config.profile.profiles.rendering.sky.cloud.CloudC

class SkyC(profile: RenderingProfile) {

    /**
     * Biomes to query when calculating sky color
     */
    var biomeRadius by IntDelegate(profile, 3, "", arrayOf(0..5))

    /**
     * Enables or disables the (ugly?) sun scatter
     */
    var sunScatter by BooleanDelegate(profile, true, "")

    val clouds = CloudC(profile)
}
