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

package de.bixilon.minosoft.modding.loader.mod.logger

import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
class ModLogger(name: String) {
    private val prefix = TextComponent("[$name] ").color(ChatColors.BLUE)

    fun fatal(message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        }
        Log.log(LogMessageType.MODS, LogLevels.FATAL, prefix, message)
    }

    fun warn(message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        }
        Log.log(LogMessageType.MODS, LogLevels.WARN, prefix, message)
    }

    fun info(message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        }
        Log.log(LogMessageType.MODS, LogLevels.INFO, prefix, message)
    }

    fun verbose(message: () -> Any?) {
        contract {
            callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        }
        Log.log(LogMessageType.MODS, LogLevels.VERBOSE, prefix, message)
    }
}
