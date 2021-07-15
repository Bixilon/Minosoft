/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.text.events

import com.google.gson.JsonObject
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

class ClickEvent {
    val action: ClickEventActions
    val value: Any

    constructor(json: JsonObject) {
        action = ClickEventActions[json["action"].asString.lowercase()]
        val primitive = json["value"].asJsonPrimitive
        value = if (primitive.isNumber) {
            primitive.asNumber
        } else {
            primitive.asString
        }
    }

    constructor(action: ClickEventActions, value: Any) {
        this.action = action
        this.value = value
    }

    enum class ClickEventActions {
        OPEN_URL,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE,
        ;

        companion object : ValuesEnum<ClickEventActions> {
            override val VALUES: Array<ClickEventActions> = values()
            override val NAME_MAP: Map<String, ClickEventActions> = KUtil.getEnumValues(VALUES)
        }
    }
}
