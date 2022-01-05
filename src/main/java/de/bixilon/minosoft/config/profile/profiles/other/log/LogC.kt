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

package de.bixilon.minosoft.config.profile.profiles.other.log

import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager.mapDelegate
import de.bixilon.minosoft.util.logging.LogMessageType

class LogC {

    /**
     * Hides various messages (e.g. chunk receiving, entity position updates, ...)
     * Only relevant if packet logging is on VERBOSE
     */
    var reducedProtocolLog by delegate(true)

    /**
     * All log message types mapped to its log level
     * @see de.bixilon.minosoft.util.logging.LogLevels
     * @see de.bixilon.minosoft.util.logging.LogMessageType
     */
    var levels by mapDelegate(LogMessageType.DEFAULT_LOG_MAP.toMutableMap())
        private set
}
