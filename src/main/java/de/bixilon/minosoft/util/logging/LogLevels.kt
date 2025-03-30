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

package de.bixilon.minosoft.util.logging

import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor

enum class LogLevels(val levelColors: RGBAColor, val error: Boolean = false) {
    FATAL(Colors.DARK_RED.rgba(), true),
    WARN(ChatColors.RED, true),
    INFO(ChatColors.WHITE),
    VERBOSE(ChatColors.YELLOW),
    ;
}
