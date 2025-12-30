/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
import de.bixilon.minosoft.config.profile.delegate.types.EnumDelegate
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile
import de.bixilon.minosoft.gui.rendering.sky.clouds.generator.CloudGenerators
import de.bixilon.minosoft.gui.rendering.sky.clouds.mesh.CloudStyle

class CloudC(profile: RenderingProfile) {

    /**
     * Renders clouds
     */
    var enabled by BooleanDelegate(profile, true)

    var style by EnumDelegate(profile, CloudStyle.VOLUME, CloudStyle)

    /**
     * Moves clouds from time to time
     */
    var movement by BooleanDelegate(profile, true)

    var generator by EnumDelegate(profile, CloudGenerators.TEXTURE, CloudGenerators)
}
