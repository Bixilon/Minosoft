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

package de.bixilon.minosoft.config.profile.profiles.other

import de.bixilon.kutil.json.JsonUtil.asMutableJsonObject

object OtherProfileMigration {

    /**
     * log level `AUDIO_LOADING` got renamed to just `AUDIO`
     */
    fun migrate1(data: MutableMap<String, Any?>) {
        data["log"]?.asMutableJsonObject()?.get("levels")?.asMutableJsonObject()?.let { it.remove("AUDIO_LOADING")?.let { audio -> it["AUDIO"] = audio } }
    }
}
