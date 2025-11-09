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

package de.bixilon.minosoft.gui.eros.dialog.session

import de.bixilon.kutil.latch.CallbackLatch
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.eros.dialog.progress.ProgressDialog
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates.Companion.disconnected
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeFX

class LoadingDialog(
    latch: CallbackLatch,
    session: PlaySession,
) : ProgressDialog(title = TITLE, header = HEADER, latch = latch) {

    init {
        session::state.observeFX(this) { if (it == PlaySessionStates.ESTABLISHING || it.disconnected) close() }
    }

    companion object {
        private val TITLE = minosoft("session.dialog.loading.title")
        private val HEADER = minosoft("session.dialog.loading.header")
    }
}
