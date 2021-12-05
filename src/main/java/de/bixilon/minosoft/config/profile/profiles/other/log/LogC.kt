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
