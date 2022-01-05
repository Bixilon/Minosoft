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

package de.bixilon.minosoft.config.profile.profiles.rendering.performance

import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager.delegate

class PerformanceC {

    /**
     * Does not render the bottom bedrock face when at minimum y.
     * Kind of xray when falling out of the world.
     */
    var fastBedrock by delegate(true)

    /**
     * Disables the voronoi noise for biome cache building.
     * Biomes may not match anymore.
     * If true, chunk receiving is way faster.
     * Only affects 19w36+ (~1.14.4)
     * ToDo: Requires rejoin to apply
     */
    var fastBiomeNoise by delegate(true)
}
