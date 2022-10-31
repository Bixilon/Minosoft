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

package de.bixilon.minosoft.data.entities.entities.player.tab

import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.data.text.ChatComponent
import java.util.*

class TabList {
    val tabListItemsByUUID: MutableMap<UUID, PlayerAdditional> = mutableMapOf()
    val tabListItemsByName: MutableMap<String, PlayerAdditional> = mutableMapOf()
    var header by watched(ChatComponent.of(""))
    var footer by watched(ChatComponent.of(""))
}
