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

package de.bixilon.minosoft.config.profile.profiles.block.lod

import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfile

class LodC(profile: BlockProfile) {

    /**
     * Enables reducing mesh details on distance
     */
    var enabled by BooleanDelegate(profile, true)

    /**
     * Hides blocks with minor visual impact in the distance (like grass, flowers, ...)
     */
    var minorVisualImpact by BooleanDelegate(profile, true)

    /**
     * Aggressively culls sides of blocks at a certain distance (e.g. leaves)
     */
    var aggressiveCulling by BooleanDelegate(profile, true)

    /**
     * Culls the sides from blocks that naturally generate in caves (deepslate, stone, ...) if their face is completely dark in the distance.
     */
    var darkCaveCulling by BooleanDelegate(profile, true)

}
