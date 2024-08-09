/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.education

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.shutdown.AbstractShutdownReason
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.minecraft.index.IndexAssetsType
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfileManager
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.types.offline.OfflineAccount
import de.bixilon.minosoft.education.config.EducationC
import de.bixilon.minosoft.education.world.EducationGenerator
import de.bixilon.minosoft.education.world.EducationStorage
import de.bixilon.minosoft.local.LocalConnection
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates.Companion.disconnected
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

object MinosoftEducation {
    val config: EducationC = EducationC()

    private fun getAccount(): Account {
        val profile = AccountProfileManager.selected
        var name = System.getenv("USER")
        if (name.isBlank()) {
            name = "unknown"
        }
        val account = OfflineAccount(name, profile.storage)
        profile.entries["education"] = account
        profile.selected = account

        return account
    }

    private fun start() {
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Starting education session..." }
        val account = getAccount()
        val version = Versions["1.16.5"]!!

        val session = PlaySession(
            connection = LocalConnection(::EducationGenerator, ::EducationStorage),
            account = account,
            version = version,
        )
        session::state.observe(this) {
            if (it.disconnected) {
                Log.log(LogMessageType.AUTO_CONNECT, LogLevels.INFO) { "Session terminated, exiting minosoft..." }
                ShutdownManager.shutdown()
            }
        }
        session::error.observe(this) { ShutdownManager.shutdown(reason = AbstractShutdownReason.CRASH) }
        session.connect()
    }

    fun setup() {
        RunConfiguration.APPLICATION_NAME = "Minosoft Education"
        RunConfiguration.IGNORE_YGGDRASIL = true
        RunConfiguration.IGNORE_MODS = true
        RunConfiguration.PROFILES_HOT_RELOADING = false
        RunConfiguration.PROFILES_SAVING = false
    }

    fun postSetup() {
        AudioProfileManager.selected.enabled = false
        AudioProfileManager.selected.skipLoading = true

        RenderingProfileManager.selected.performance.fastBedrock = false
        ResourcesProfileManager.selected.assets.indexAssetsTypes.clear()
        ResourcesProfileManager.selected.assets.indexAssetsTypes += IndexAssetsType.OTHER
        ResourcesProfileManager.selected.assets.indexAssetsTypes += IndexAssetsType.LANGUAGE
    }

    @JvmStatic
    fun main(args: Array<String>) {
        setup()
        Minosoft.main(emptyArray())
        postSetup()

        // TODO: load education.json

        start()
    }
}
