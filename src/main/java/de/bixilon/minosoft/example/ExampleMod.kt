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

package de.bixilon.minosoft.example

import de.bixilon.minosoft.assets.util.InputStreamUtil.readAsString
import de.bixilon.minosoft.modding.loader.mod.ModMain
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

object ExampleMod : ModMain() {

    override fun init() {
        Log.log(LogMessageType.GENERAL, LogLevels.INFO) { "This mod can not do anything!" }
        val message = assets["example:message.txt".toResourceLocation()].readAsString()
        Log.log(LogMessageType.GENERAL, LogLevels.INFO) { "Stored message is: $message" }
    }

    override fun postInit() {
        Log.log(LogMessageType.GENERAL, LogLevels.WARN) { "This mod can not do much yet!" }
    }
}
