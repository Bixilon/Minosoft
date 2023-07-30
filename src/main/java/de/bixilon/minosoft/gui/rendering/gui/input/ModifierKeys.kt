/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.input

import de.bixilon.minosoft.config.key.KeyCodes

enum class ModifierKeys(vararg val codes: KeyCodes) {
    CONTROL(KeyCodes.KEY_LEFT_CONTROL, KeyCodes.KEY_RIGHT_CONTROL),
    ALT(KeyCodes.KEY_LEFT_ALT, KeyCodes.KEY_RIGHT_ALT),
    SHIFT(KeyCodes.KEY_LEFT_SHIFT, KeyCodes.KEY_RIGHT_SHIFT),
    SUPER(KeyCodes.KEY_LEFT_SUPER, KeyCodes.KEY_RIGHT_SUPER),
    ;
}
