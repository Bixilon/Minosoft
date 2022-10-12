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

package de.bixilon.minosoft.config.profile.profiles.eros

import com.fasterxml.jackson.databind.JavaType
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedBiMapOf
import de.bixilon.kutil.collections.map.bi.AbstractMutableBiMap
import de.bixilon.kutil.watcher.map.bi.BiMapDataWatcher.Companion.watchedBiMap
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.delegate.delegate.BackingDelegate
import de.bixilon.minosoft.config.profile.delegate.delegate.ProfileDelegate
import de.bixilon.minosoft.config.profile.delegate.delegate.entry.ListDelegateProfile
import de.bixilon.minosoft.config.profile.delegate.delegate.entry.MapDelegateProfile
import de.bixilon.minosoft.config.profile.delegate.delegate.entry.SetDelegateProfile
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.json.Jackson
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import javafx.collections.SetChangeListener
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import java.util.concurrent.locks.ReentrantLock

object ErosProfileManager : ProfileManager<ErosProfile> {
    override val namespace = "minosoft:eros".toResourceLocation()
    override val latestVersion get() = 1
    override val saveLock = ReentrantLock()
    override val profileClass = ErosProfile::class.java
    override val jacksonProfileType: JavaType = Jackson.MAPPER.typeFactory.constructType(profileClass)
    override val profileSelectable: Boolean
        get() = false
    override val icon = FontAwesomeSolid.WINDOW_RESTORE


    override var currentLoadingPath: String? = null
    override val profiles: AbstractMutableBiMap<String, ErosProfile> by watchedBiMap(synchronizedBiMapOf())

    override var selected: ErosProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(ErosProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): ErosProfile {
        currentLoadingPath = name
        val profile = ErosProfile(description ?: "Default eros profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }

    override fun <V> delegate(value: V, verify: ((V) -> Unit)?): ProfileDelegate<V> {
        return ProfileDelegate(value, this, currentLoadingPath ?: getName(selected), verify)
    }

    override fun <V> backingDelegate(verify: ((V) -> Unit)?, getter: () -> V, setter: (V) -> Unit): BackingDelegate<V> {
        return object : BackingDelegate<V>(this, currentLoadingPath ?: getName(selected), verify) {
            override fun get(): V = getter()

            override fun set(value: V) = setter(value)
        }
    }

    override fun <K, V> mapDelegate(default: MutableMap<K, V>, verify: ((MapChangeListener.Change<out K, out V>) -> Unit)?): MapDelegateProfile<K, V> {
        return MapDelegateProfile(FXCollections.synchronizedObservableMap(FXCollections.observableMap(default)), profileManager = this, profileName = currentLoadingPath ?: getName(selected), verify = verify)
    }

    override fun <V> listDelegate(default: MutableList<V>, verify: ((ListChangeListener.Change<out V>) -> Unit)?): ListDelegateProfile<V> {
        return ListDelegateProfile(FXCollections.synchronizedObservableList(FXCollections.observableList(default)), profileManager = this, profileName = currentLoadingPath ?: getName(selected), verify = verify)
    }

    override fun <V> setDelegate(default: MutableSet<V>, verify: ((SetChangeListener.Change<out V>) -> Unit)?): SetDelegateProfile<V> {
        return SetDelegateProfile(FXCollections.synchronizedObservableSet(FXCollections.observableSet(default)), profileManager = this, profileName = currentLoadingPath ?: getName(selected), verify = verify)
    }
}
