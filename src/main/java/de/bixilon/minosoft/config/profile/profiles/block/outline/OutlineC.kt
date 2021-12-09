/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.config.profile.profiles.block.BlockProfileManager.delegate
import de.bixilon.minosoft.data.text.ChatColors

class OutlineC {

    /**
     * Highlights the current selected block
     */
    var enabled by delegate(true)

    /**
     * Shows the collision box of the selected block
     */
    var showCollisionBoxes by delegate(false)

    /**
     * Disables the z-buffer of the block outline
     * Makes the whole outline visible and ignores the walls
     */
    var showThroughWalls by delegate(false)

    /**
     * The color of the block that is currently selected
     * Defaults to light red
     */
    var outlineColor by delegate(ChatColors.RED)

    /**
     * The color of the block collision box that is currently selected
     * Defaults to light blue
     */
    var collisionColor by delegate(ChatColors.RED)
}
