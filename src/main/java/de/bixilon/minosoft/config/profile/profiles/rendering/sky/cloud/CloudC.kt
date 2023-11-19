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

package de.bixilon.minosoft.config.profile.profiles.rendering.sky.cloud

import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.primitive.FloatDelegate
import de.bixilon.minosoft.config.profile.delegate.primitive.IntDelegate
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile

class CloudC(profile: RenderingProfile) {

    /**
     * Renders clouds
     */
    var enabled by BooleanDelegate(profile, true, "")

    /**
     * Renders clouds flat and not 3d
     */
    var flat by BooleanDelegate(profile, false, "")

    /**
     * Moves clouds from time to time
     */
    var movement by BooleanDelegate(profile, true, "")

    /**
     * Max y axis distance to clouds
     */
    var maxDistance by FloatDelegate(profile, 60.0f, "")


    /**
     * Number of cloud layers
     */
    var layers by IntDelegate(profile, 3, arrayOf(0..10))
}
