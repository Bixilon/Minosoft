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

package de.bixilon.minosoft.data.text

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.util.KUtil.asResourceLocation

object TranslatableComponents {
   val GENERAL_EMPTY = "minosoft:general.empty".asResourceLocation()
   val GENERAL_CANCEL = "minosoft:general.cancel".asResourceLocation()
   val GENERAL_CONFIRM = "minosoft:general.confirm".asResourceLocation()
   val GENERAL_DELETE = "minosoft:general.delete".asResourceLocation()

   val EROS_DELETE_SERVER_CONFIRM_DESCRIPTION = { name: ChatComponent, address: String -> Minosoft.LANGUAGE_MANAGER.translate("minosoft:server_info.delete.dialog.description".asResourceLocation(), null, name, address) }
   val ACCOUNT_CARD_CONNECTION_COUNT = { count: Int -> Minosoft.LANGUAGE_MANAGER.translate("minosoft:main.account.card.connection_count".asResourceLocation(), null, count) }
}
