package de.bixilon.minosoft.data.scoreboard

import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

enum class ScoreboardPositions {
    LIST,
    SIDEBAR,
    BELOW_NAME,
    TEAM_BLACK,
    TEAM_DARK_BLUE,
    TEAM_DARK_GREEN,
    TEAM_DARK_AQUA,
    TEAM_DARK_RED,
    TEAM_DARK_PURPLE,
    TEAM_GOLD,
    TEAM_GRAY,
    TEAM_DARK_GRAY,
    TEAM_BLUE,
    TEAM_GREEN,
    TEAM_AQUA,
    TEAM_RED,
    TEAM_PURPLE,
    TEAM_YELLOW,
    TEAM_WHITE,
    ;

    companion object : ValuesEnum<ScoreboardPositions> {
        override val VALUES: Array<ScoreboardPositions> = values()
        override val NAME_MAP: Map<String, ScoreboardPositions> = KUtil.getEnumValues(VALUES)
    }
}
