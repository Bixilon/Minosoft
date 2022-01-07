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

package de.bixilon.minosoft.config.profile.profiles.entity.hitbox

import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfileManager.delegate

class HitboxC {
    /**
     * Enables or disables hit-boxes for all entities
     */
    var enabled by delegate(true)

    /**
     * Shows your own hit-box when in first person view
     */
    var showLocal by delegate(false)

    /**
     * Shows hit-boxes from invisible entities
     */
    var showInvisible by delegate(false)

    /**
     * If true: Shows full colored hit-boxes (aka. lazy boxes).
     * If false: Shows just the outline of the hit-box
     */
    var lazy by delegate(false)

    /**
     * Disables the z-buffer when rendering
     * => Shows the boxes through walls
     */
    var showThroughWalls by delegate(false)
}
