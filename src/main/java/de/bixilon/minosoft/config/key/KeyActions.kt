/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.key

enum class KeyAction {
    // primitive ones

    /**
     * Key is normally pressed on the keyboard
     */
    PRESS,

    /**
     * Key is normally released on the keyboard
     */
    RELEASE,

    /**
     * Key must be pressed or released
     */
    CHANGE,

    // custom ones

    /**
     * Key must be hold in addition to a other action
     */
    MODIFIER,

    /**
     * ToDo: Key must be pressed twice
     */
    DOUBLE_CLICK,

    /**
     * Pressing the key makes it sticky, you have to press it again to make it not pressed anymore
     */
    STICKY,

    /**
     * Exactly the same as STICKY, but inverted. Initial pressed
     */
    STICKY_INVERTED,
    ;

    companion object {
        val VALUES = values()
    }
}
