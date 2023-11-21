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

package de.bixilon.minosoft.config.profile.profiles.other.log

import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.types.map.MapDelegate
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfile
import de.bixilon.minosoft.util.logging.LogMessageType

class LogC(profile: OtherProfile) {

    /**
     * Hides various messages (e.g. chunk receiving, entity position updates, ...)
     * Only relevant if packet logging is on VERBOSE
     */
    var reducedProtocolLog by BooleanDelegate(profile, true)

    /**
     * All log message types mapped to its log level
     * @see de.bixilon.minosoft.util.logging.LogLevels
     * @see de.bixilon.minosoft.util.logging.LogMessageType
     */
    var levels by MapDelegate(profile, LogMessageType.DEFAULT_LOG_MAP.toMutableMap())
        private set
}
