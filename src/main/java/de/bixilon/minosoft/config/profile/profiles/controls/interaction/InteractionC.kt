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

package de.bixilon.minosoft.config.profile.profiles.controls.interaction

import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfileManager.delegate

class InteractionC {

    /**
     * Enables or disables right-clicking with a shovel on grass (…) to create grass paths
     */
    var flattening by delegate(true)

    /**
     * Enables right-clicking with an axe on any logs to create stripped logs
     */
    var stripping by delegate(true)

    /**
     * Enables right-clicking with a hoe on grass (…) to create farmland
     */
    var tilling by delegate(true)
}
