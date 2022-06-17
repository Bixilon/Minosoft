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
package de.bixilon.minosoft.config

object StaticConfiguration {
    const val VERSION = "0.1-pre"
    const val DEBUG_MODE = true // if true, additional checks will be made to validate data, ... Decreases performance
    const val DEBUG_SLOW_LOADING = false // if true, many Thread.sleep will be executed and the start will be delayed (by a lot)
    const val SHOW_LOG_MESSAGES_IN_CHAT = false // prints all console messages in the chat box
    const val REPLACE_SYSTEM_OUT_STREAMS = true // Replace System.out and System.err with the custom Log system ones

    const val LOG_DELEGATE = false


    const val IGNORE_SERVER_LIGHT = true
}
