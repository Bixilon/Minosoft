/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.text

import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.AbstractServer
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.language.IntegratedLanguage
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft

object TranslatableComponents {
    val GENERAL_EMPTY = minosoft("general.empty")
    val GENERAL_CANCEL = minosoft("general.cancel")
    val GENERAL_CONFIRM = minosoft("general.confirm")
    val GENERAL_DELETE = minosoft("general.delete")
    val GENERAL_IGNORE = minosoft("general.ignore")
    val GENERAL_EXIT = minosoft("general.exit")
    val GENERAL_REFRESH = minosoft("general.refresh")

    @Deprecated("yah")
    val EROS_DELETE_SERVER_CONFIRM_DESCRIPTION = { name: ChatComponent, address: String -> IntegratedLanguage.LANGUAGE.forceTranslate(minosoft("server_info.delete.dialog.description"), name, address) }

    @Deprecated("yah")
    val ACCOUNT_CARD_CONNECTION_COUNT = { count: Int -> IntegratedLanguage.LANGUAGE.forceTranslate(minosoft("main.account.card.session_count"), count) }

    @Deprecated("yah")
    val CONNECTION_KICK_DESCRIPTION = { server: AbstractServer, account: Account -> IntegratedLanguage.LANGUAGE.forceTranslate(minosoft("session.kick.description"), server.name, account.username) }

    @Deprecated("yah")
    val CONNECTION_LOGIN_KICK_DESCRIPTION = { server: AbstractServer, account: Account -> IntegratedLanguage.LANGUAGE.forceTranslate(minosoft("session.login_kick.description"), server.name, account.username) }
}
