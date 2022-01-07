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

package de.bixilon.minosoft.config.profile.delegate.delegate.entry

import de.bixilon.kutil.watcher.WatchUtil.identifier
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

open class ListDelegateProfile<V>(
    private var value: ObservableList<V>,
    profileManager: ProfileManager<*>,
    profileName: String,
    private val verify: ((ListChangeListener.Change<out V>) -> Unit)?,
) : ProfileEntryDelegate<MutableList<V>>(profileManager, profileName) {

    init {
        initListener()
    }

    private fun initListener() {
        value.addListener(ListChangeListener {
            verify?.invoke(it)
            checkLateinitValues(null)

            if (!this.profileInitialized || profile.initializing) {
                return@ListChangeListener
            }
            if (StaticConfiguration.LOG_DELEGATE) {
                Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Changed list entry $it in profile $profileName" }
            }
            if (!profile.reloading) {
                profileManager.profiles[profileName]?.saved = false
            }

            ProfilesDelegateManager.onChange(profile, property.identifier, null, it)
        })
    }

    override fun get(): MutableList<V> = value

    override fun set(value: MutableList<V>) {
        this.value = FXCollections.synchronizedObservableList(FXCollections.observableList(value))
        initListener()
        if (!profile.reloading) {
            profileManager.profiles[profileName]?.saved = false
        }
    }
}
