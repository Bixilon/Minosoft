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

package de.bixilon.minosoft.terminal.arguments.connect

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.shutdown.AbstractShutdownReason
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.kutil.string.WhitespaceUtil.removeWhitespaces
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.types.offline.OfflineAccount
import de.bixilon.minosoft.gui.eros.ErosOptions
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates.Companion.disconnected
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class AutoConnectArgument(val connect: () -> AutoConnectFactory?) : OptionGroup() {
    val version by option("--protocol-version").default(Versions.AUTOMATIC.name) // Versions might not be loaded yet
    val account by option("--account")


    private fun getAccount(): Account {
        if (this.account == null) {
            var name = System.getProperty("user.name").removeWhitespaces()
            if (name.isBlank()) {
                name = "unknown"
            }
            Log.log(LogMessageType.AUTO_CONNECT, LogLevels.INFO) { "No account specified, using temporary offline account: $name" }
            return OfflineAccount(name, null)
        }
        val profile = AccountProfileManager.selected
        val account = profile.entries[this.account] ?: profile.selected ?: throw IllegalArgumentException("Account ${this.account} not found! Did your create one before?")

        Log.log(LogMessageType.AUTO_CONNECT, LogLevels.INFO) { "Checking account $account..." }
        account.tryCheck(null)

        return account
    }


    fun connect() {
        val connect = connect.invoke() ?: return
        val account = getAccount()
        val version = Versions[version] ?: throw IllegalArgumentException("No such protocol version: $version")
        val session = connect.create(version, account)

        if (ErosOptions.disabled) {
            session::state.observe(this) {
                if (it.disconnected) {
                    Log.log(LogMessageType.AUTO_CONNECT, LogLevels.INFO) { "Disconnected from server, exiting..." }
                    ShutdownManager.shutdown()
                }
            }
            session::error.observe(this) { ShutdownManager.shutdown(reason = AbstractShutdownReason.CRASH) }
        }
        session.connect()
    }
}
