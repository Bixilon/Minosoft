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

package de.bixilon.minosoft.config.profile.profiles.rendering.advanced

import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager.delegate

class AdvancedC {

    /**
     * Sets the window swap interval (vsync)
     * 0 means vsync disabled
     * Every value above 0 means 1/x  * <vsync framerate>
     * Must not be negative
     */
    var swapInterval by delegate(1) { check(it >= 0) { "Swap interval must not be negative!" } }
}
