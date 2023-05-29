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

package de.bixilon.minosoft.gui.eros.dialog.connection

import de.bixilon.kutil.latch.CallbackLatch
import de.bixilon.minosoft.gui.eros.dialog.progress.ProgressDialog
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates.Companion.disconnected
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeFX

class LoadingDialog(
    latch: CallbackLatch,
    connection: PlayConnection,
) : ProgressDialog(title = TITLE, header = HEADER, latch = latch) {

    init {
        connection::state.observeFX(this) { if (it == PlayConnectionStates.ESTABLISHING || it.disconnected) close() }
    }

    companion object {
        private val TITLE = "minosoft:connection.dialog.loading.title".toResourceLocation()
        private val HEADER = "minosoft:connection.dialog.loading.header".toResourceLocation()
    }
}
