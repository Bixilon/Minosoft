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

package de.bixilon.minosoft.config.profile.profiles.block.outline

import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.types.ColorDelegate
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfile
import de.bixilon.minosoft.data.text.formatting.color.ChatColors

class OutlineC(profile: BlockProfile) {

    /**
     * Highlights the current selected block
     */
    var enabled by BooleanDelegate(profile, true, "profile.block.outline.enabled")


    /**
     * Disables the z-buffer of the block outline
     * Makes the whole outline visible and ignores the walls
     */
    var showThroughWalls by BooleanDelegate(profile, false, "profile.block.outline.through_walls")

    /**
     * The color of the block that is currently selected
     * Defaults to light red
     */
    var outlineColor by ColorDelegate(profile, ChatColors.RED, "profile.block.outline.color")


    /**
     * Shows the collision box of the selected block
     */
    var collisions by BooleanDelegate(profile, false, "profile.block.outline.collisions.enabled")

    /**
     * The color of the block collision box that is currently selected
     */
    var collisionColor by ColorDelegate(profile, ChatColors.BLUE, "profile.block.outline.collisions.color")
}
