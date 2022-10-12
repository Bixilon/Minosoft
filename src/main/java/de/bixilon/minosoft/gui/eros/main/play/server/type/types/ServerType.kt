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

package de.bixilon.minosoft.gui.eros.main.play.server.type.types

import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.Server
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCard
import org.kordamp.ikonli.Ikon

interface ServerType : Translatable {
    val icon: Ikon
    val hidden: Boolean
    var readOnly: Boolean

    val servers: MutableList<Server>

    fun refresh(cards: List<ServerCard>)


    companion object {
        val TYPES = setOf(
            CustomServerType,
            LANServerType,
        )
    }
}
