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

package de.bixilon.minosoft.gui.rendering.input.interaction

enum class InteractionResults {
    /**
     * Usage get consumed (like trying to open an iron door) without animation
     */
    CONSUME,

    /**
     * Usage get consumed (like pressing a button, opening a door, right clicking on block entities, placing a block, …) with animation
     */
    SUCCESS,

    /**
     * Nothing happens from block side (e.g. right clicking on dirt). You can maybe place a block, whatever
     */
    PASS,


    /**
     * Like consume, but with an error (no packet will be sent to the server)
     */
    ERROR,
    ;
}
